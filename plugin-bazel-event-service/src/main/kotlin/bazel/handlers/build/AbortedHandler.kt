package bazel.handlers.build

import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class AbortedHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasAborted()) {
            return false
        }
        val aborted = ctx.event.aborted
        if (ctx.event.id.hasTargetConfigured()) {
            ctx.targetRegistry.getTarget(ctx.event.id)?.let { target ->
                val reason = formatAbortReason(aborted.reason)
                if (target.description.isNotEmpty()) {
                    ctx.onNext(
                        MessageFactory
                            .createMessage(
                                ctx
                                    .buildMessage(false)
                                    .append(target.description)
                                    .append(" aborted.".apply(Color.Error))
                                    .append(" $reason")
                                    .append(if (aborted.description.isNotBlank()) ": ${aborted.description}" else ".")
                                    .toString(),
                            ),
                    )
                }
            }
        }
        return true
    }

    private fun formatAbortReason(reason: BuildEventStreamProtos.Aborted.AbortReason?): String =
        when (reason) {
            BuildEventStreamProtos.Aborted.AbortReason.USER_INTERRUPTED -> "The user interrupted the build (e.g., Ctrl-C)"
            BuildEventStreamProtos.Aborted.AbortReason.TIME_OUT -> "Timeout exceeded"
            BuildEventStreamProtos.Aborted.AbortReason.REMOTE_ENVIRONMENT_FAILURE -> "Remote environment failure"
            BuildEventStreamProtos.Aborted.AbortReason.INTERNAL -> "Internal failure (e.g., crash)"
            BuildEventStreamProtos.Aborted.AbortReason.LOADING_FAILURE -> "Failure during loading phase"
            BuildEventStreamProtos.Aborted.AbortReason.ANALYSIS_FAILURE -> "Failure during analysis phase"
            BuildEventStreamProtos.Aborted.AbortReason.SKIPPED -> "Target was skipped"
            BuildEventStreamProtos.Aborted.AbortReason.NO_ANALYZE -> "No analysis requested"
            BuildEventStreamProtos.Aborted.AbortReason.NO_BUILD -> "No build requested"
            BuildEventStreamProtos.Aborted.AbortReason.INCOMPLETE -> "Build incomplete due to earlier failure"
            BuildEventStreamProtos.Aborted.AbortReason.OUT_OF_MEMORY -> "Out of memory"
            else -> "Unknown reason"
        }
}
