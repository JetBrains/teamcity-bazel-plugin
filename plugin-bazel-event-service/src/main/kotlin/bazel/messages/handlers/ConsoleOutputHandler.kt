package bazel.messages.handlers

import bazel.messages.ServiceMessageContext

class ConsoleOutputHandler : EventHandler {
    override fun handle(ctx: ServiceMessageContext) = ctx.event.rawEvent.hasConsoleOutput()
}
