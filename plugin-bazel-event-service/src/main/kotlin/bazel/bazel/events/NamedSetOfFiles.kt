package bazel.bazel.events

// Payload of a message to describe a set of files, usually build artifacts, to
// be referred to later by their name. In this way, files that occur identically
// as outputs of several targets have to be named only once.

data class NamedSetOfFiles(
        override val id: Id,
        override val children: List<Id>,
        // Files that belong to this named set of files.
        val files: MutableList<File>) : BazelContent