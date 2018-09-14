package bazel.events

// Notification that an invocation attempt has finished.
data class InvocationAttemptFinished(
        override val streamId: StreamId,
        override val sequenceNumber: Long,
        override val eventTime: Timestamp,
        // Final status of the invocation.
        val invocationResult: Result,
        // The exit code of the build tool.
        val exitCode: Int)
    : OrderedBuildEvent