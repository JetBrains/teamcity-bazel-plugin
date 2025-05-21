package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.bazel.converters.TestStatusConverter
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class TestSummaryHandler : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.Low

    private val testStatusConverter = TestStatusConverter()

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        if (payload is BazelEvent && payload.event.hasTestSummary() && payload.event.id.hasTestSummary()) {
            val summary = payload.event.testSummary
            val label = payload.event.id.testSummary.label

            val overallStatus = testStatusConverter.convert(summary.overallStatus)
            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("$label test summary:")
                        .append(" ${overallStatus.name}".apply(overallStatus.toColor()))
                        .append(", total run count: ${summary.totalRunCount}", Verbosity.Detailed)
                        .append(", total cached: ${summary.totalNumCached}".apply(Color.Details), Verbosity.Verbose)
                        .toString(),
                ),
            )

            return true
        }
        return ctx.handlerIterator.next().handle(ctx)
    }
}
