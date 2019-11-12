package bazel.bazel.events

data class WorkspaceConfig(
        override val id: Id,
        override val children: List<Id>,
        val localExecRoot: String) : BazelContent