package bazel.bazel.events

import bazel.events.OrderedBuildEvent
import bazel.events.StreamId
import bazel.events.Timestamp

data class BazelEvent(
        override val streamId: StreamId,
        override val sequenceNumber: Long,
        override val eventTime: Timestamp,
        // Message describing a build event. Events will have an identifier that
        // is unique within a given build invocation; they also announce follow-up
        // events as children. More details, which are specific to the kind of event
        // that is observed, is provided in the payload. More options for the payload
        // might be added in the future.
        val content: BazelContent)
    : OrderedBuildEvent