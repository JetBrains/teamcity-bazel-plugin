package bazel.messages.handlers

import bazel.messages.BazelEventHandlerContext

class ExecRequestHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext) = ctx.bazelEvent.hasExecRequest()
}
