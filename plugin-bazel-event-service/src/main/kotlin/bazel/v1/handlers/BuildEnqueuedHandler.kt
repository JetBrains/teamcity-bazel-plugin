package bazel.v1.handlers

import bazel.HandlerPriority
import bazel.events.BuildEnqueued
import bazel.events.OrderedBuildEvent

class BuildEnqueuedHandler: EventHandler {
    override val priority: HandlerPriority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext): OrderedBuildEvent =
            if (ctx.event.hasBuildEnqueued()) {
                BuildEnqueued(
                        ctx.streamId,
                        ctx.sequenceNumber,
                        ctx.eventTime)
            } else ctx.handlerIterator.next().handle(ctx)
}