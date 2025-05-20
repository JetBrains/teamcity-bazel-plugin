package bazel.v1.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.converters.BazelEventConverter
import bazel.bazel.events.BazelContent
import bazel.bazel.events.BazelEvent
import bazel.events.OrderedBuildEvent
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import java.util.logging.Level
import java.util.logging.Logger

class BazelEventHandler(
    private val _bazelEventConverter: Converter<BuildEventStreamProtos.BuildEvent, BazelContent>,
) : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.High

    override fun handle(ctx: HandlerContext): OrderedBuildEvent {
        if (!ctx.event.hasBazelEvent()) {
            return ctx.handlerIterator.next().handle(ctx)
        }
        val bazelEvent = ctx.event.bazelEvent
        if (bazelEvent.typeUrl != "type.googleapis.com/build_event_stream.BuildEvent") {
            logger.log(Level.SEVERE, "Unknown bazel event: ${bazelEvent.typeUrl}")
            return ctx.handlerIterator.next().handle(ctx)
        }

        val event = bazelEvent.unpack(BuildEventStreamProtos.BuildEvent::class.java)
        val content = _bazelEventConverter.convert(event)
        return BazelEvent(
            ctx.streamId,
            ctx.sequenceNumber,
            ctx.eventTime,
            content,
            event,
        )
    }

    companion object {
        private val logger = Logger.getLogger(BazelEventConverter::class.java.name)
    }
}
