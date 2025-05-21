package bazel.messages.handlers

import bazel.messages.BazelEventHandlerContext

class TargetSummaryHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext) = ctx.bazelEvent.hasTargetSummary()
}
