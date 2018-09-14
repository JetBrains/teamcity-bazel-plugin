package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.*
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class FetchHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is Fetch) {
            val event = ctx.event.payload.content
            if (event.success) {
                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append("Fetch \"${event.url}\"".apply(Color.Details))
                                    .toString()))
                }
            }
            else {
                if (ctx.verbosity.atLeast(Verbosity.Minimal)) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append("Fetch \"${event.url}\" - unsuccessful".apply(Color.Error))
                                    .toString()))
                }
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}