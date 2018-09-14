package bazel.v1.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.BazelContent
import bazel.bazel.events.BazelEvent
import bazel.events.OrderedBuildEvent
import com.google.protobuf.Any

class BazelEventHandler(
        private val _bazelEventConverter: Converter<Any, BazelContent>)
    : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.High

    override fun handle(ctx: HandlerContext): OrderedBuildEvent =
            if (ctx.event.hasBazelEvent()) {
                val bazelEvent = ctx.event.bazelEvent
                val content = _bazelEventConverter.convert(bazelEvent)
                BazelEvent(
                        ctx.streamId,
                        ctx.sequenceNumber,
                        ctx.eventTime,
                        content)
            } else ctx.handlerIterator.next().handle(ctx)
}