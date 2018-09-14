package bazel.bazel.events

// Payload of the event indicating the completion of a target. The target is
// specified in the id. If the target failed the root causes are provided as
// children events.

data class TargetComplete(
        override val id: Id,
        override val children: List<Id>,
        val label: String,
        val success: Boolean,
        // List of tags associated with this configured target.
        val tags: List<String>,
        // The timeout specified for test actions under this configured target.
        val testTimeoutSeconds: Long) : BazelContent