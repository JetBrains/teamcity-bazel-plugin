package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped

class TargetCompletedHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasCompleted()) {
            return false
        }

        val completed = ctx.event.completed
        ctx.hierarchy.tryCloseNode(ctx.event.id)?.let {
            val description =
                ctx
                    .buildMessage()
                    .append(it.description)
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
                ctx.onNext(ctx.messageFactory.createMessage(description))
            }

            if (!completed.success) {
                ctx.onNext(ctx.messageFactory.createErrorMessage(description))
            }
        }

        return true
    }
}
