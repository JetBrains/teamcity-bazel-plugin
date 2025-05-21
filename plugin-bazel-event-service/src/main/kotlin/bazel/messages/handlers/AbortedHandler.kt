package bazel.messages.handlers

import bazel.bazel.events.Id
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.Aborted.AbortReason.*

class AbortedHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasAborted()) {
            return false
        }
        val aborted = ctx.bazelEvent.aborted
        val reason = formatAbortReason(aborted.reason)
        ctx.hierarchy.tryAbortNode(ctx, Id(ctx.bazelEvent.id))?.let {
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
