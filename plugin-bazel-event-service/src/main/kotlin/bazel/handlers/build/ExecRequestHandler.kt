package bazel.handlers.build

import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult

class ExecRequestHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext) = HandlerResult(handled = ctx.event.hasExecRequest(), emptySequence())
}
