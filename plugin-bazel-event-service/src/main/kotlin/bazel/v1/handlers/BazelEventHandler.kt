

package bazel.v1.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.converters.BazelEventConverter
import bazel.bazel.events.BazelContent
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.BazelUnknownContent
import bazel.events.OrderedBuildEvent
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import java.util.logging.Level
import java.util.logging.Logger

class BazelEventHandler(
    private val _bazelEventConverter: Converter<BuildEventStreamProtos.BuildEvent, BazelContent>,
) : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.High

    override fun handle(ctx: HandlerContext): OrderedBuildEvent =
        if (ctx.event.hasBazelEvent()) {
            val bazelEvent = ctx.event.bazelEvent
            val content =
                when (bazelEvent.typeUrl) {
                    "type.googleapis.com/build_event_stream.BuildEvent" -> {
                        val event = bazelEvent.unpack(BuildEventStreamProtos.BuildEvent::class.java)
                        _bazelEventConverter.convert(event)
                    }

                    else -> {
                        logger.log(Level.SEVERE, "Unknown bazel event: ${bazelEvent.typeUrl}")
                        BazelUnknownContent.default
                    }
                }

            BazelEvent(
                ctx.streamId,
                ctx.sequenceNumber,
                ctx.eventTime,
                content,
            )
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }

    companion object {
        private val logger = Logger.getLogger(BazelEventConverter::class.java.name)
    }
}
