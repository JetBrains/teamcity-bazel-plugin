package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.TargetComplete
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class TargetCompletedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is TargetComplete) {
                val event = ctx.event.payload.content
                ctx.hierarchy.tryCloseNode(ctx, event.id)?.let {
                    val description = ctx.buildMessage()
                            .append(it.description)
                            .append(
                                    if (event.success) {
                                        " completed"
                                    } else {
                                        " failed".apply(Color.Error)
                                    })
                            .append(", test timeout: ${event.testTimeoutSeconds}(seconds)", Verbosity.Verbose) { event.testTimeoutSeconds != 0L }
                            .append(", tags: \"${event.tags.joinToStringEscaped(", ")}\"", Verbosity.Verbose) { event.tags.isNotEmpty() }
                            .toString()

                    if (event.success) {
                        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                            ctx.onNext(ctx.messageFactory.createMessage(description))
                        }
                    } else {
                        ctx.onNext(ctx.messageFactory.createErrorMessage(description))
                    }
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)
}