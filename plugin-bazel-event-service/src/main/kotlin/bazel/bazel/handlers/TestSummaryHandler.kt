package bazel.bazel.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.File
import bazel.bazel.events.TestStatus
import bazel.bazel.events.TestSummary
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class TestSummaryHandler(
        private val _fileConverter: Converter<BuildEventStreamProtos.File, File>,
        private val _testStatusConverter: Converter<BuildEventStreamProtos.TestStatus, TestStatus>)
    : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasTestSummary() && ctx.event.hasId() && ctx.event.id.hasTestSummary()) {
                val content = ctx.event.testSummary
                val passed = mutableListOf<File>()
                for (i in 0 until content.passedCount) {
                    passed.add(_fileConverter.convert(content.getPassed(i)))
                }

                val failed = mutableListOf<File>()
                for (i in 0 until content.failedCount) {
                    failed.add(_fileConverter.convert(content.getFailed(i)))
                }

                TestSummary(
                        ctx.id,
                        ctx.children,
                        ctx.event.id.testSummary.label,
                        _testStatusConverter.convert(content.overallStatus),
                        content.totalRunCount,
                        passed,
                        failed,
                        content.totalNumCached)
            } else ctx.handlerIterator.next().handle(ctx)
}