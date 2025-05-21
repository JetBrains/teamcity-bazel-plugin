package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.Id
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.Aborted.AbortReason.*

class AbortedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        if (payload is BazelEvent && payload.event.hasAborted()) {
            val aborted = payload.event.aborted
            val reason = formatAbortReason(aborted.reason)
            ctx.hierarchy.tryAbortNode(ctx, Id(payload.event.id))?.let {
                if (it.description.isNotEmpty()) {
                    ctx.onNext(
                        ctx.messageFactory
                            .createMessage(
                                ctx
                                    .buildMessage(false)
                                    .append(it.description)
                                    .append(" aborted.".apply(Color.Error))
                                    .append(" $reason")
                                    .append(if (aborted.description.isNotBlank()) ": ${aborted.description}" else ".")
                                    .toString(),
                            ),
                    )
                }
            }

            return true
        } else {
            return ctx.handlerIterator.next().handle(ctx)
        }
    }

    private fun formatAbortReason(reason: BuildEventStreamProtos.Aborted.AbortReason?): String =
        when (reason) {
            USER_INTERRUPTED -> "The user interrupted the build (e.g., Ctrl-C)"
            TIME_OUT -> "Timeout exceeded"
            REMOTE_ENVIRONMENT_FAILURE -> "Remote environment failure"
            INTERNAL -> "Internal failure (e.g., crash)"
            LOADING_FAILURE -> "Failure during loading phase"
            ANALYSIS_FAILURE -> "Failure during analysis phase"
            SKIPPED -> "Target was skipped"
            NO_ANALYZE -> "No analysis requested"
            NO_BUILD -> "No build requested"
            INCOMPLETE -> "Build incomplete due to earlier failure"
            OUT_OF_MEMORY -> "Out of memory"
            else -> "Unknown reason"
        }
}
