package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.TestStatusConverter
import bazel.messages.apply
import bazel.messages.toColor

class TestSummaryHandler : BuildEventHandler {
    private val testStatusConverter = TestStatusConverter()

    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasTestSummary() || !ctx.event.id.hasTestSummary()) {
            return false
        }

        val summary = ctx.event.testSummary
        val label = ctx.event.id.testSummary.label

        val overallStatus = testStatusConverter.convert(summary.overallStatus)
        ctx.writer.message(
            buildString {
                append("$label test summary:")
                append(" ${overallStatus.name}".apply(overallStatus.toColor()))
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    append(", total run count: ${summary.totalRunCount}")
                }
                if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    append(", total cached: ${summary.totalNumCached}".apply(Color.Details))
                }
            },
        )

        return true
    }
}
