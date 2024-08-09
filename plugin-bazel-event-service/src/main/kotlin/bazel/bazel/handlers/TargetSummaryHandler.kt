

package bazel.bazel.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.TargetSummary
import bazel.bazel.events.TestStatus
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class TargetSummaryHandler(private val _testStatusConverter: Converter<BuildEventStreamProtos.TestStatus, TestStatus>) : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasTargetSummary()) {
                val content = ctx.event.targetSummary
                TargetSummary(
                        ctx.id,
                        ctx.children,
                        content.overallBuildSuccess,
                        _testStatusConverter.convert(content.overallTestStatus))
            } else ctx.handlerIterator.next().handle(ctx)
}