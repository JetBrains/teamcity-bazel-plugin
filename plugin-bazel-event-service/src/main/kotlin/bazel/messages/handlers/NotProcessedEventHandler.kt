package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.messages.ServiceMessageContext

class NotProcessedEventHandler: EventHandler {
    override val priority: HandlerPriority get() = HandlerPriority.Last

    override fun handle(ctx: ServiceMessageContext): Boolean {
        return false
    }
}