package bazel.handlers.build

import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext

class TestProgressHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext) = ctx.event.hasTestProgress()
}
