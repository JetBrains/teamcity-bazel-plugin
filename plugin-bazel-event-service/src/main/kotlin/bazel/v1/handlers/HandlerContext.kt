package bazel.v1.handlers

import bazel.events.StreamId
import bazel.events.Timestamp
import com.google.devtools.build.v1.BuildEvent

data class HandlerContext(
        val handlerIterator: Iterator<EventHandler>,
        val streamId: StreamId,
        val sequenceNumber: Long,
        val eventTime: Timestamp,
        val event: BuildEvent)