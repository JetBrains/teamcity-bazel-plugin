package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped

class TargetCompletedHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasCompleted()) {
            return false
        }

        val completed = ctx.event.completed
        ctx.targetRegistry.getTarget(ctx.event.id)?.let { target ->
            val description =
                ctx
                    .buildMessage()
                    .append(target.description)
                    .append(
                        if (completed.success) {
                            " completed"
                        } else {
                            " failed".apply(Color.Error)
                        },
                    ).append(
                        ", test timeout: ${completed.testTimeout.seconds}(seconds)",
                        Verbosity.Verbose,
                    ) { completed.testTimeout.seconds != 0L }
                    .append(
                        ", tags: \"${completed.tagList.joinToStringEscaped(", ")}\"",
                        Verbosity.Verbose,
                    ) { completed.tagList.isNotEmpty() }
                    .toString()

            if (completed.success && ctx.verbosity.atLeast(Verbosity.Detailed)) {
                ctx.emitMessage(MessageFactory.createMessage(description))
            }

            if (!completed.success) {
                ctx.emitMessage(MessageFactory.createErrorMessage(description))
            }
        }

        return true
    }
}
