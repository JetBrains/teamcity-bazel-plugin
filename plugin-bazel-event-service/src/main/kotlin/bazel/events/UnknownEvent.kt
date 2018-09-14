package bazel.events

data class UnknownEvent(
        override val streamId: StreamId)
    : OrderedBuildEvent {
    override val sequenceNumber: Long get() = -1
    override val eventTime: Timestamp get() = Timestamp.zero
}