package bazel.v1.handlers

import bazel.HandlerPriority
import bazel.events.InvocationAttemptStarted
import bazel.events.OrderedBuildEvent

class InvocationAttemptStartedHandler: EventHandler {
    override val priority: HandlerPriority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext): OrderedBuildEvent =
        if (ctx.event.hasInvocationAttemptStarted()) {
            InvocationAttemptStarted(
                    ctx.streamId,
                    ctx.sequenceNumber,
                    ctx.eventTime,
                    ctx.event.invocationAttemptStarted.attemptNumber)
        } else ctx.handlerIterator.next().handle(ctx)
}