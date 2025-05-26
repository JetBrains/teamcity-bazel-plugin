package bazel.messages.handlers

import bazel.Verbosity
import bazel.bazel.converters.TestStatusConverter
import bazel.bazel.events.toColor
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class TestSummaryHandler : BazelEventHandler {
    private val testStatusConverter = TestStatusConverter()

    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasTestSummary() || !ctx.bazelEvent.id.hasTestSummary()) {
            return false
        }

        val summary = ctx.bazelEvent.testSummary
        val label = ctx.bazelEvent.id.testSummary.label

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
