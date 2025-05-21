package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class BuildMetricsHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        return if (payload is BazelEvent && payload.event.hasBuildMetrics()) {
            val event = payload.event.buildMetrics
            if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
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
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
    }
}
