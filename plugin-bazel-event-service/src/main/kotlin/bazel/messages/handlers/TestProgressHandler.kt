package bazel.messages.handlers

import bazel.messages.BazelEventHandlerContext

class TestProgressHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext) = ctx.bazelEvent.hasTestProgress()
}
