package bazel.bazel.events

class BazelUnknownContent(
        override val id: Id,
        override val children: List<Id>): BazelContent {

    companion object {
        val default = BazelUnknownContent(Id.default, emptyList())
    }
}