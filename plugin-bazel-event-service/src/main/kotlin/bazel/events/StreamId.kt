package bazel.events

// Unique identifier for a build event stream.

data class StreamId(
        // The id of a Build message.
        val buildId: String,
        // The unique invocation ID within this build.
        // It should be the same as {invocation} (below) during the migration.
        val invocationId: String,
        // The component that emitted this event.
        val component: BuildComponent) {
    companion object {
        val default = StreamId("", "", BuildComponent.UnknownComponent)
    }
}