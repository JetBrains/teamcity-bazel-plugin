package bazel.handlers.bep

import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext

class TestProgressHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext) = ctx.event.hasTestProgress()
}
