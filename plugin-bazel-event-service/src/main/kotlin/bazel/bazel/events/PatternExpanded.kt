package bazel.bazel.events

// Payload of the event indicating the expansion of a target pattern.
// The main information is in the chaining part: the id will contain the
// target pattern that was expanded and the children id will contain the
// target or target pattern it was expanded to.

class PatternExpanded(
        override val id: Id,
        override val children: List<Id>,
        val patterns: List<String>): BazelContent