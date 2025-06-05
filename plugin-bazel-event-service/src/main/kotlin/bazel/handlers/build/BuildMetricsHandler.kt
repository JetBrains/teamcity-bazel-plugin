package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createMessage
import bazel.messages.apply

class BuildMetricsHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasBuildMetrics()) {
            return notHandled()
        }

        return handled(
            sequence {
                if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    return@sequence
                }

                val event = ctx.event.buildMetrics
                val actionsCreated = if (event.hasActionSummary()) event.actionSummary.actionsCreated else 0
                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append("Actions created: $actionsCreated".apply(Color.Details))
                        },
                    ),
                )

                val usedHeapSizePostBuild =
                    if (event.hasMemoryMetrics()) event.memoryMetrics.usedHeapSizePostBuild else 0
                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append("Used heap size post build: $usedHeapSizePostBuild".apply(Color.Details))
                        },
                    ),
                )
            },
        )
    }
}
