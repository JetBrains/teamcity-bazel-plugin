package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class FetchHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        return if (payload is BazelEvent && payload.rawEvent.hasFetch()) {
            val event = payload.rawEvent.fetch

            val url =
                if (payload.rawEvent.hasId() && payload.rawEvent.id.hasFetch()) {
                    payload.rawEvent.id.fetch.url
                } else {
                    "unknown url"
                }

            if (event.success) {
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    ctx.onNext(
                        ctx.messageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append("Fetch \"${url}\"")
                                .toString(),
                        ),
                    )
                }
            } else {
                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    ctx.onNext(
                        ctx.messageFactory.createWarningMessage(
                            ctx
                                .buildMessage()
                                .append("Fetch \"${url}\" - unsuccessful".apply(Color.Error))
                                .toString(),
                        ),
                    )
                }
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
    }
}
