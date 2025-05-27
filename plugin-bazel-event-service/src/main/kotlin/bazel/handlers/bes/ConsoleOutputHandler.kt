package bazel.handlers.bes

import bazel.handlers.BesEventHandler
import bazel.handlers.BesEventHandlerContext

class ConsoleOutputHandler : BesEventHandler {
    override fun handle(ctx: BesEventHandlerContext) = ctx.event.hasConsoleOutput()
}
