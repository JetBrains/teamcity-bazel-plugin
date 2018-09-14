package bazel.v1.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.events.InvocationAttemptFinished
import bazel.events.OrderedBuildEvent
import bazel.events.Result
import com.google.devtools.build.v1.BuildStatus

class InvocationAttemptFinishedHandler(
        private val _buildStatusConverter: Converter<BuildStatus, Result>)
    : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext): OrderedBuildEvent =
            if (ctx.event.hasInvocationAttemptFinished()) {
                InvocationAttemptFinished(
                        ctx.streamId,
                        ctx.sequenceNumber,
                        ctx.eventTime,
                        if (ctx.event.invocationAttemptFinished.hasInvocationStatus()) _buildStatusConverter.convert(ctx.event.invocationAttemptFinished.invocationStatus) else Result.default,
                        if (ctx.event.invocationAttemptFinished.hasExitCode()) ctx.event.invocationAttemptFinished.exitCode.value else 0)
            } else ctx.handlerIterator.next().handle(ctx)
}