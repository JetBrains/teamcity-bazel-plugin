package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.*
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class BuildMetricsHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is BuildMetrics) {
            val event = ctx.event.payload.content
            if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                ctx.onNext(ctx.messageFactory.createMessage(
                        ctx.buildMessage()
                                .append("Actions created: ${event.actionsCreated}".apply(Color.Details))
                                .toString()))

                ctx.onNext(ctx.messageFactory.createMessage(
                        ctx.buildMessage()
                                .append("Used heap size post build: ${event.usedHeapSizePostBuild}".apply(Color.Details))
                                .toString()))
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}