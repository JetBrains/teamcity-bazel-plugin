package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class BuildMetricsHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasBuildMetrics()) {
            return false
        }
        if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
            return true
        }

        val event = ctx.bazelEvent.buildMetrics
        val actionsCreated = if (event.hasActionSummary()) event.actionSummary.actionsCreated else 0
        val usedHeapSizePostBuild =
            if (event.hasMemoryMetrics()) event.memoryMetrics.usedHeapSizePostBuild else 0

        ctx.onNext(
            ctx.messageFactory.createMessage(
                ctx
                    .buildMessage()
                    .append("Actions created: $actionsCreated".apply(Color.Details))
                    .toString(),
            ),
        )

        ctx.onNext(
            ctx.messageFactory.createMessage(
                ctx
                    .buildMessage()
                    .append("Used heap size post build: $usedHeapSizePostBuild".apply(Color.Details))
                    .toString(),
            ),
        )

        return true
    }
}
