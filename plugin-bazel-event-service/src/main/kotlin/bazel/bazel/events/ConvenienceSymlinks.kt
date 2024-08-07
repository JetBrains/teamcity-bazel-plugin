

package bazel.bazel.events

data class ConvenienceSymlinks(
        override val id: Id,
        override val children: List<Id>,
        val symlinks: List<ConvenienceSymlink>
) : BazelContent

data class ConvenienceSymlink(val path: String, val action: ConvenienceSymlinkAction, val target: String)

enum class ConvenienceSymlinkAction {
        Unknown,
        Create,
        Delete,
        Unrecognized
}