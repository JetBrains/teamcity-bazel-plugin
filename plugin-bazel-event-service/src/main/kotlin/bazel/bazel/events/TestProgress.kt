

package bazel.bazel.events

data class TestProgress(
        override val id: Id,
        override val children: List<Id>,
        val uri: String
) : BazelContent