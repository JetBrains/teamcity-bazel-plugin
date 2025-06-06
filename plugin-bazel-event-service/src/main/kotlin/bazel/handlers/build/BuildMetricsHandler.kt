package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class BuildMetricsHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildMetrics()) {
            return false
        }

        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            val event = ctx.event.buildMetrics
            val actionsCreated = if (event.hasActionSummary()) event.actionSummary.actionsCreated else 0
            ctx.writer.message("Actions created: $actionsCreated".apply(Color.Details))

            val heapSize = if (event.hasMemoryMetrics()) event.memoryMetrics.usedHeapSizePostBuild else 0
            ctx.writer.message("Used heap size post build: $heapSize".apply(Color.Details))
        }

        return true
    }
}
