package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.Id
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class TargetCompletedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        if (payload is BazelEvent && payload.event.hasCompleted()) {
            val completed = payload.event.completed

            ctx.hierarchy.tryCloseNode(ctx, Id(payload.event.id))?.let {
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

                if (completed.success) {
                    if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                        ctx.onNext(ctx.messageFactory.createMessage(description))
                    }
                } else {
                    ctx.onNext(ctx.messageFactory.createErrorMessage(description))
                }
            }

            return true
        }
        return ctx.handlerIterator.next().handle(ctx)
    }
}
