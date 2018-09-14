package bazel.bazel.events

interface BazelContent {
    val id: Id
    val children: List<Id>
}