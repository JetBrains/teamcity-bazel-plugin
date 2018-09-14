package bazel.v1.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.events.ComponentStreamFinished
import bazel.events.FinishType
import bazel.events.OrderedBuildEvent
import com.google.devtools.build.v1.BuildEvent

class ComponentStreamFinishedHandler(
        private val _finishTypeConverter: Converter<BuildEvent.BuildComponentStreamFinished.FinishType, FinishType>)
    : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext): OrderedBuildEvent =
            if (ctx.event.hasComponentStreamFinished()) {
                ComponentStreamFinished(
                        ctx.streamId,
                        ctx.sequenceNumber,
                        ctx.eventTime,
                        _finishTypeConverter.convert(ctx.event.componentStreamFinished.type))
            } else ctx.handlerIterator.next().handle(ctx)
}