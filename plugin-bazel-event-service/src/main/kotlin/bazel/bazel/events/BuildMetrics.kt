package bazel.bazel.events

data class BuildMetrics(
        override val id: Id,
        override val children: List<Id>,
        val actionsCreated: Long,
        val usedHeapSizePostBuild: Long) : BazelContent