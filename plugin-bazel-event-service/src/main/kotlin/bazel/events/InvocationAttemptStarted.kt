package bazel.events

// An invocation attempt has started.

data class InvocationAttemptStarted(
        override val streamId: StreamId,
        override val sequenceNumber: Long,
        override val eventTime: Timestamp,
        // The number of the invocation attempt, starting at 1 and increasing by 1
        // for each new attempt. Can be used to determine if there is a later
        // invocation attempt replacing the current one a client is processing.
        val attemptNumber: Long)
    : OrderedBuildEvent