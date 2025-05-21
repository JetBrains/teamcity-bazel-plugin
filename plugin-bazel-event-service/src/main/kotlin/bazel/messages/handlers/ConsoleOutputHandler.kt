package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.messages.ServiceMessageContext

class ConsoleOutputHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) = ctx.event.rawEvent.hasConsoleOutput()
}
