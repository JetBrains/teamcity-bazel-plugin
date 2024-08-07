

package bazel.bazel.events

data class TargetSummary(
        override val id: Id,
        override val children: List<Id>,
        val overallBuildSuccess: Boolean,
        val overallTestStatus: TestStatus
) : BazelContent