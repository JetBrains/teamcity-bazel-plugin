package bazel.events

interface OrderedBuildEvent {
    val streamId: StreamId
    val sequenceNumber: Long
    val eventTime: Timestamp
}