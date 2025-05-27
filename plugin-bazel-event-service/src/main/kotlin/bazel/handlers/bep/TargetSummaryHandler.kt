package bazel.handlers.bep

import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext

class TargetSummaryHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext) = ctx.event.hasTargetSummary()
}
