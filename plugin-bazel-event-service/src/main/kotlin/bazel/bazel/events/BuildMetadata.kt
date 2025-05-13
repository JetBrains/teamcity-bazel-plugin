

package bazel.bazel.events

data class BuildMetadata(
    override val id: Id,
    override val children: List<Id>,
    val metadata: Map<String, String>,
) : BazelContent
