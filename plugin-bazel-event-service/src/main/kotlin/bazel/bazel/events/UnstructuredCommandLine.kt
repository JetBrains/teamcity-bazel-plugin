package bazel.bazel.events

// Payload of an event reporting the command-line of the invocation as
// originally received by the server. Note that this is not the command-line
// given by the user, as the client adds information about the invocation,
// like name and relevant entries of rc-files and client environment variables.
// However, it does contain enough information to reproduce the build
// invocation.

data class UnstructuredCommandLine(
        override val id: Id,
        override val children: List<Id>,
        val args: List<String>) : BazelContent