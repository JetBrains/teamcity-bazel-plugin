package bazel.events

// Notification that the build request has finished, and no further
// invocations will occur.  Note that this applies to the entire Build.
// Individual invocations trigger InvocationFinished when they finish.

data class BuildFinished(
        override val streamId: StreamId,
        override val sequenceNumber: Long,
        override val eventTime: Timestamp,
        // Final status of the build.
        val result: Result)
    : OrderedBuildEvent