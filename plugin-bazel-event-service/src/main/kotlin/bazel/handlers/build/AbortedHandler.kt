package bazel.handlers.build

import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createMessage
import bazel.messages.TargetRegistry
import bazel.messages.apply
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos.Aborted.AbortReason.*

class AbortedHandler(
    private val targetRegistry: TargetRegistry,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasAborted()) {
            return notHandled()
        }

        return handled(
            sequence {
                val aborted = ctx.event.aborted
                if (!ctx.event.id.hasTargetConfigured()) {
                    return@sequence
                }
                val target = targetRegistry.getTarget(ctx.event.id)?.description
                if (target.isNullOrEmpty()) {
                    return@sequence
                }
                yield(
                    createMessage(
                        buildString {
                            append(target)
                            append(" aborted.".apply(Color.Error))
                            append(" ${formatAbortReason(aborted.reason)}")
                            append(if (aborted.description.isNotBlank()) ": ${aborted.description}" else ".")
                        },
                    ),
                )
            },
        )
    }

    private fun formatAbortReason(reason: BuildEventStreamProtos.Aborted.AbortReason?) =
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
