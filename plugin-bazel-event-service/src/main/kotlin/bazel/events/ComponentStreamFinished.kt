package bazel.events

// Notification of the end of a build event stream published by a build
// component other than CONTROLLER (See StreamId.BuildComponents).

data class ComponentStreamFinished(
        override val streamId: StreamId,
        override val sequenceNumber: Long,
        override val eventTime: Timestamp,
        // How did the event stream finish.
        val finishType: FinishType)
    : OrderedBuildEvent