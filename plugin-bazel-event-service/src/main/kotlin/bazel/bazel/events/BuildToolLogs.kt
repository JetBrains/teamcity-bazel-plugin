package bazel.bazel.events

// Event providing additional statistics/logs after completion of the build.

data class BuildToolLogs(
        override val id: Id,
        override val children: List<Id>,
        val logs: MutableList<File>) : BazelContent