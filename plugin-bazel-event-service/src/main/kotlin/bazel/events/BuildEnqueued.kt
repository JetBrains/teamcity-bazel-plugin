package bazel.events

// Notification that the build request is enqueued. It could happen when
// a new build request is inserted into the build queue, or when a
// build request is put back into the build queue due to a previous build
// failure.

data class BuildEnqueued(
        override val streamId: StreamId,
        override val sequenceNumber: Long,
        override val eventTime: Timestamp)
    : OrderedBuildEvent