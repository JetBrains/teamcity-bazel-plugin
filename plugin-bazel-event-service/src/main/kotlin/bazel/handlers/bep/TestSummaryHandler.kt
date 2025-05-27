package bazel.handlers.bep

import bazel.Verbosity
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.TestStatusConverter
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.toColor

class TestSummaryHandler : BepEventHandler {
    private val testStatusConverter = TestStatusConverter()

    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasTestSummary() || !ctx.event.id.hasTestSummary()) {
            return false
        }

        val summary = ctx.event.testSummary
        val label = ctx.event.id.testSummary.label

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
}
