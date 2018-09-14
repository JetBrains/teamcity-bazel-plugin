package bazel.v1.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.events.BuildFinished
import bazel.events.OrderedBuildEvent
import bazel.events.Result
import com.google.devtools.build.v1.BuildStatus

class BuildFinishedHandler(
        private val _buildStatusConverter: Converter<BuildStatus, Result>)
    : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext): OrderedBuildEvent =
            if (ctx.event.hasBuildFinished()) {
                BuildFinished(
                        ctx.streamId,
                        ctx.sequenceNumber,
                        ctx.eventTime,
                        if (ctx.event.buildFinished.hasStatus()) _buildStatusConverter.convert(ctx.event.buildFinished.status) else Result.default)
            } else ctx.handlerIterator.next().handle(ctx)
}