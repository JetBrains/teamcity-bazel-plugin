package bazel.handlers.build

import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext

class TargetSummaryHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext) = ctx.event.hasTargetSummary()
}
