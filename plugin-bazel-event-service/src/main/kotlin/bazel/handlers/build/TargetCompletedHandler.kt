package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createErrorMessage
import bazel.messages.MessageFactory.createMessage
import bazel.messages.TargetRegistry
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class TargetCompletedHandler(
    private val targetRegistry: TargetRegistry,
) : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasCompleted()) {
            return notHandled()
        }
        return handled(
            sequence {
                val completed = ctx.event.completed
                targetRegistry.getTarget(ctx.event.id)?.let { target ->
                    val description =
                        buildString {
                            append(ctx.messagePrefix)
                            append(target.description)
                            if (completed.success) {
                                append(" completed")
                            } else {
                                append(" failed".apply(Color.Error))
                            }
                            if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                                if (completed.testTimeout.seconds != 0L) {
                                    append(", test timeout: ${completed.testTimeout.seconds}(seconds)")
                                }
                                if (completed.tagList.isNotEmpty()) {
                                    append(", tags: \"${completed.tagList.joinToStringEscaped(", ")}\"")
                                }
                            }
                        }

                    if (completed.success && ctx.verbosity.atLeast(Verbosity.Detailed)) {
                        yield(createMessage(description))
                    }

                    if (!completed.success) {
                        yield(createErrorMessage(description))
                    }
                }
            },
        )
    }
}
