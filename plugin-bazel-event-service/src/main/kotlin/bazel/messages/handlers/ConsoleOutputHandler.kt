package bazel.messages.handlers

import bazel.messages.BuildEventHandlerContext

class ConsoleOutputHandler : EventHandler {
    override fun handle(ctx: BuildEventHandlerContext) = ctx.event.hasConsoleOutput()
}
