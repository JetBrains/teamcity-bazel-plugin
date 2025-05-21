package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.Id
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class TargetCompletedHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasCompleted()) {
            return false
        }

        val completed = ctx.bazelEvent.completed
        ctx.hierarchy.tryCloseNode(Id(ctx.bazelEvent.id))?.let {
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
