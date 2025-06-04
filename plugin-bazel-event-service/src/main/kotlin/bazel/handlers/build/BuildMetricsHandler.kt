package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage

class BuildMetricsHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildMetrics()) {
            return false
        }
        if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
            return true
        }

        val event = ctx.event.buildMetrics
        val actionsCreated = if (event.hasActionSummary()) event.actionSummary.actionsCreated else 0
        val usedHeapSizePostBuild =
            if (event.hasMemoryMetrics()) event.memoryMetrics.usedHeapSizePostBuild else 0

        ctx.emitMessage(
            MessageFactory.createMessage(
                ctx
                    .buildMessage()
                    .append("Actions created: $actionsCreated".apply(Color.Details))
                    .toString(),
            ),
        )

        ctx.emitMessage(
            MessageFactory.createMessage(
                ctx
                    .buildMessage()
                    .append("Used heap size post build: $usedHeapSizePostBuild".apply(Color.Details))
                    .toString(),
            ),
        )

        return true
    }
}
