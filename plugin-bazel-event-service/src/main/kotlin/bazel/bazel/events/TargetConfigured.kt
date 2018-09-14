package bazel.bazel.events

// Payload of the event indicating that the configurations for a target have
// been identified. As with pattern expansion the main information is in the
// chaining part: the id will contain the target that was configured and the
// children id will contain the configured targets it was configured to.

data class TargetConfigured(
        override val id: Id,
        override val children: List<Id>,
        val label: String,
        val aspect: String,
        // The kind of target (e.g.,  e.g. "cc_library rule", "source file",
        // "generated file") where the completion is reported.
        val targetKind: String,
        // The size of the test, if the target is a test target. Unset otherwise.
        val testSize: TestSize,
        // List of all tags associated with this target (for all possible configurations).
        val tags: List<String>) : BazelContent