package bazel.bazel.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.File
import bazel.bazel.events.TestResult
import bazel.bazel.events.TestStatus
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class TestResultHandler(
        private val _fileConverter: Converter<BuildEventStreamProtos.File, File>,
        private val _testStatusConverter: Converter<BuildEventStreamProtos.TestStatus, TestStatus>)
    : BazelHandler {
    override val priority = HandlerPriority.High

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasTestResult() && ctx.event.hasId() && ctx.event.id.hasTestResult()) {
                val content = ctx.event.testResult
                val testActionOutput = mutableListOf<File>()
                for (i in 0 until content.testActionOutputCount) {
                    testActionOutput.add(_fileConverter.convert(content.getTestActionOutput(i)))
                }

                val warnings = mutableListOf<String>()
                for (i in 0 until content.warningCount) {
                    warnings.add(content.getWarning(i))
                }

                TestResult(
                        ctx.id,
                        ctx.children,
                        ctx.event.id.testResult.label,
                        ctx.event.id.testResult.run,
                        ctx.event.id.testResult.shard,
                        ctx.event.id.testResult.attempt,
                        _testStatusConverter.convert(content.status),
                        content.statusDetails,
                        content.cachedLocally,
                        content.testAttemptStartMillisEpoch,
                        content.testAttemptDurationMillis,
                        testActionOutput,
                        warnings)
            } else ctx.handlerIterator.next().handle(ctx)
}