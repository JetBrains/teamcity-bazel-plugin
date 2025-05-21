package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.bazel.events.BazelEvent
import bazel.messages.ServiceMessageContext

class ExecRequestHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        return if (payload is BazelEvent && payload.event.hasExecRequest()) {
            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
    }
}
