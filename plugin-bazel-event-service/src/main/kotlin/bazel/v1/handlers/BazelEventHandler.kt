package bazel.v1.handlers

import bazel.HandlerPriority
import bazel.bazel.events.BazelEvent
import bazel.events.OrderedBuildEvent
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import java.util.logging.Level
import java.util.logging.Logger

class BazelEventHandler : EventHandler {
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
        return BazelEvent(
            ctx.streamId,
            ctx.sequenceNumber,
            ctx.eventTime,
            event,
        )
    }

    companion object {
        private val logger = Logger.getLogger(BazelEventHandler::class.java.name)
    }
}
