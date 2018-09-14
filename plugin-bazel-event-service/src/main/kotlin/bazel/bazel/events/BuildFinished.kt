package bazel.bazel.events

// Event indicating the end of a build.

data class BuildFinished(
        override val id: Id,
        override val children: List<Id>,
        // A build was successful iff ExitCode.code equals 0.
        // The exit code.
        val exitCode: Int,
        // The name of the exit code.
        val exitCodeName: String) : BazelContent