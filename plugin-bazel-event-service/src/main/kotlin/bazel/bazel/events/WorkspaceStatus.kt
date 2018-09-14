package bazel.bazel.events

// Payload of an event reporting the workspace status. Key-value pairs can be
// provided by specifying the workspace_status_command to an executable that
// returns one key-value pair per line of output (key and value separated by a
// space).

data class WorkspaceStatus(
        override val id: Id,
        override val children: List<Id>,
        val items: Map<String, String>) : BazelContent