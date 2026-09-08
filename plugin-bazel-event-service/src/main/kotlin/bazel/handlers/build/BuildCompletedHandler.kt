package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.CommandNameContext
import bazel.messages.PendingInvocationDiagnostics
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.BuildFinished.ExitCode

class BuildCompletedHandler(
    private val context: CommandNameContext,
    private val pendingDiagnostics: PendingInvocationDiagnostics,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasFinished()) {
            return false
        }

        if (ctx.verbosity.atLeast(Verbosity.Normal)) {
            ctx.writer.blockClosed(context.commandName)
        }

        val event = ctx.event.finished
        when (event.exitCode.code) {
            0 ->
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    ctx.writer.message("Build completed, exit code ${event.exitCode}")
                }

            3 -> ctx.writer.message("Build completed with failed test(s), exit code ${event.exitCode}")
            4 -> ctx.writer.message("No tests were found, exit code ${event.exitCode}")
            // The only exit code a Bazel-driven retry follows, so the only one worth withholding.
            REMOTE_CACHE_EVICTED -> pendingDiagnostics.addRetriableFailure(ctx.writer, buildFailed(event.exitCode))
            else -> ctx.writer.error(buildFailed(event.exitCode))
        }

        return true
    }

    private fun buildFailed(exitCode: ExitCode) = "Build failed: ${exitCode.name}, exit code $exitCode"

    private companion object {
        const val REMOTE_CACHE_EVICTED = 39
    }
}
