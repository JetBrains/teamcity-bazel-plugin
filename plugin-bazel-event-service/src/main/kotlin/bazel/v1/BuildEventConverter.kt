package bazel.v1

import bazel.Event
import bazel.events.OrderedBuildEvent
import bazel.events.StreamId
import bazel.events.Timestamp
import bazel.events.UnknownEvent
import bazel.v1.converters.StreamIdConverter
import com.google.devtools.build.v1.BuildEvent
import java.util.logging.Level
import java.util.logging.Logger

class BuildEventConverter(
    private val _streamIdConverter: StreamIdConverter,
) {
    fun convert(source: Event<com.google.devtools.build.v1.OrderedBuildEvent>): Event<OrderedBuildEvent> {
        val payload = source.payload
        val streamId = if (payload.hasStreamId()) _streamIdConverter.convert(payload.streamId) else StreamId.default
        if (payload.hasEvent()) {
            val event: BuildEvent = payload.event
            val sequenceNumber = payload.sequenceNumber
            val eventTime = Timestamp(event.eventTime.seconds, event.eventTime.nanos)
            val orderedEvent =
                object : OrderedBuildEvent {
                    override val streamId = streamId
                    override val sequenceNumber = sequenceNumber
                    override val eventTime = eventTime
                }
            return Event(source.projectId, orderedEvent, event)
        }

        logger.log(Level.SEVERE, "Unknown event: $source")
        return Event(source.projectId, UnknownEvent(streamId), rawEvent = BuildEvent.getDefaultInstance())
    }

    companion object {
        private val logger = Logger.getLogger(BuildEventConverter::class.java.name)
    }
}
