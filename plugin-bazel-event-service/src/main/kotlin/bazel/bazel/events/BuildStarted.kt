package bazel.bazel.events

// Payload of an event indicating the beginning of a new build. Usually, events
// of those type start a new build-event stream. The target pattern requested
// to be build is contained in one of the announced child events; it is an
// invariant that precisely one of the announced child events has a non-empty
// target pattern.

data class BuildStarted(
        override val id: Id,
        override val children: List<Id>,
        // Version of the build tool that is running.
        val buildToolVersion: String,
        // The name of the command that the user invoked.
        val command: String,
        // The working directory from which the build tool was invoked.
        val workingDirectory: String,
        // The directory of the workspace.
        val workspaceDirectory: String) : BazelContent