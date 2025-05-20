package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.bazel.events.BazelEvent
import bazel.messages.ServiceMessageContext

class TestProgressHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        return if (payload is BazelEvent && payload.rawEvent.hasTestProgress()) {
            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
    }
}
