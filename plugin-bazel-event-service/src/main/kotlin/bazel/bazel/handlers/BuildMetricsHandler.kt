package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.BuildMetrics

class BuildMetricsHandler: BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasBuildMetrics()) {
                val content = ctx.event.buildMetrics
                val actionsCreated= if (content.hasActionSummary()) content.actionSummary.actionsCreated else 0
                val usedHeapSizePostBuild= if (content.hasMemoryMetrics()) content.memoryMetrics.usedHeapSizePostBuild else 0
                BuildMetrics(
                        ctx.id,
                        ctx.children,
                        actionsCreated,
                        usedHeapSizePostBuild)
            } else ctx.handlerIterator.next().handle(ctx)
}