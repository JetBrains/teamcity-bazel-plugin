package bazel.messages

/**
 * Buffers the failure diagnostics of the invocation that is currently being read.
 *
 * Bazel restarts an invocation on its own after a transient remote cache error
 * (`REMOTE_CACHE_EVICTED`, exit code 39), which `--experimental_remote_cache_eviction_retries`
 * retries by default; the retry usually succeeds and `bazel` exits with 0. A TeamCity compilation
 * error is a build problem and cannot be retracted once written, so reporting a failure as soon as
 * it is seen permanently fails builds that Bazel went on to complete successfully.
 *
 * Diagnostics are therefore held until the invocation is known to be the last one: written out when
 * the event stream ends, dropped when Bazel restarts an invocation that failed the way it retries,
 * and reported when the next invocation starts if it ended any other way — a server handling builds
 * in sequence must not lose the earlier ones.
 *
 * The bounds keep the deferral from costing what immediate reporting did not: reports used to be
 * released as soon as they were written, so a build with very many failed actions could not
 * accumulate their output on the heap.
 */
class PendingInvocationDiagnostics {
    private val lock = Any()
    private val pending = mutableListOf<Diagnostic>()
    private var bufferedDetailChars = 0
    private var dropped = 0
    private var bazelRetries = false
    private val finishedFlows = mutableListOf<FinishedFlow>()

    /** [details] is only evaluated when there is room for it, since producing it reads files. */
    fun addCompilationError(
        writer: MessageWriter,
        summary: String,
        details: () -> String,
    ) = synchronized(lock) { buffer { Diagnostic.CompilationError(writer, summary, charged(details)) } }

    fun addErrorMessage(
        writer: MessageWriter,
        text: String,
        hasPrefix: Boolean = true,
    ) = synchronized(lock) { buffer { Diagnostic.ErrorMessage(writer, charged { text }, hasPrefix) } }

    /**
     * For a `BuildFinished` carrying the exit code Bazel retries: [text] is kept in case this
     * attempt turns out to be the last one, and the flag marks a `BuildStarted` that follows as a
     * retry rather than as another build.
     */
    fun addRetriableFailure(
        writer: MessageWriter,
        text: String,
    ) = synchronized(lock) {
        bazelRetries = true
        buffer { Diagnostic.ErrorMessage(writer, charged { text }, hasPrefix = true) }
    }

    /** Keep the invocation's flow open until its diagnostics have been reported or discarded. */
    fun deferFlowFinished(
        writer: MessageWriter,
        flowId: String,
    ) = synchronized(lock) {
        finishedFlows.add(FinishedFlow(writer, flowId))
    }

    /**
     * The previous invocation is over. If it ended the way Bazel retries, this one supersedes it and
     * its diagnostics are dropped; otherwise it was a separate build and they are reported now.
     */
    fun onInvocationStarted(writer: MessageWriter) {
        val buffered = take()
        if (buffered.bazelRetries) {
            discard(buffered, writer)
        } else {
            report(buffered, writer)
        }
    }

    /** For a restart established outside the event stream, which needs no exit code to confirm it. */
    fun discardSuperseded(writer: MessageWriter) = discard(take(), writer)

    /**
     * Call once the event stream is over: only then is the last invocation known to be the last.
     *
     * The retriable flag is ignored on purpose — a stream that ends after such an attempt means the
     * retries were exhausted, so the failure is Bazel's own verdict.
     */
    fun flush(writer: MessageWriter) = report(take(), writer)

    private fun take(): Buffered =
        synchronized(lock) {
            Buffered(pending.toList(), dropped, bazelRetries, finishedFlows.toList()).also { reset() }
        }

    private fun discard(
        buffered: Buffered,
        writer: MessageWriter,
    ) {
        if (buffered.count > 0) {
            writer.warning(
                "Bazel restarted the invocation; " +
                    "${buffered.count} failure(s) reported by the superseded attempt are ignored.",
            )
        }
        buffered.finishedFlows.forEach { it.report() }
    }

    private fun report(
        buffered: Buffered,
        writer: MessageWriter,
    ) {
        buffered.diagnostics.forEach {
            when (it) {
                is Diagnostic.CompilationError -> {
                    it.writer.compilationStarted(it.summary)
                    it.writer.error(it.details, hasPrefix = false)
                    it.writer.compilationFinished(it.summary)
                }

                is Diagnostic.ErrorMessage -> it.writer.error(it.text, hasPrefix = it.hasPrefix)
            }
        }

        if (buffered.dropped > 0) {
            writer.warning(
                "${buffered.dropped} further failure(s) were not reported: " +
                    "more than $MAX_BUFFERED_REPORTS failures in a single invocation.",
            )
        }
        buffered.finishedFlows.forEach { it.report() }
    }

    private inline fun buffer(diagnostic: () -> Diagnostic) {
        if (pending.size >= MAX_BUFFERED_REPORTS) {
            dropped++
        } else {
            pending.add(diagnostic())
        }
    }

    /**
     * Charges what [text] produces against the shared budget, truncating once it is spent.
     *
     * Once the budget is gone [text] is not called at all, which is what spares [addCompilationError]
     * the reading of output files whose content it could not keep anyway.
     */
    private fun charged(text: () -> String): String {
        val room = MAX_BUFFERED_DETAIL_CHARS - bufferedDetailChars
        if (room <= 0) {
            return OMITTED_DETAILS
        }

        val produced = text()
        val kept = if (produced.length > room) produced.take(room) + TRUNCATION_MARKER else produced
        bufferedDetailChars += kept.length
        return kept
    }

    private fun reset() {
        pending.clear()
        bufferedDetailChars = 0
        dropped = 0
        bazelRetries = false
        finishedFlows.clear()
    }

    private class Buffered(
        val diagnostics: List<Diagnostic>,
        val dropped: Int,
        val bazelRetries: Boolean,
        val finishedFlows: List<FinishedFlow>,
    ) {
        val count: Int
            get() = diagnostics.size + dropped
    }

    private sealed interface Diagnostic {
        /** Becomes a TeamCity build problem, which is what makes premature reporting unrecoverable. */
        data class CompilationError(
            val writer: MessageWriter,
            val summary: String,
            val details: String,
        ) : Diagnostic

        data class ErrorMessage(
            val writer: MessageWriter,
            val text: String,
            val hasPrefix: Boolean,
        ) : Diagnostic
    }

    private class FinishedFlow(
        val writer: MessageWriter,
        val id: String,
    ) {
        fun report() = writer.flowFinished(id)
    }

    private companion object {
        const val MAX_BUFFERED_REPORTS = 500
        const val MAX_BUFFERED_DETAIL_CHARS = 4 * 1024 * 1024
        const val TRUNCATION_MARKER = "\n<truncated>"
        const val OMITTED_DETAILS = "<omitted: buffered failure output limit reached>"
    }
}
