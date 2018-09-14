package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.TestSummary
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class TestSummaryHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is TestSummary) {
            val event = ctx.event.payload.content
            ctx.onNext(ctx.messageFactory.createMessage(
                    ctx.buildMessage()
                            .append("${event.label} test summary:")
                            .append(" ${event.overallStatus}".apply(event.overallStatus.toColor()))
                            .append(", total: ${event.totalRunCount}", Verbosity.Normal)
                            .append(", total cached: ${event.totalNumCached}".apply(Color.Details), Verbosity.Detailed)
                            .toString()))

            true
        } else ctx.handlerIterator.next().handle(ctx)
}