package bazel.bazel.events

// Payload of an event indicating that an external resource was fetched. This
// event will only occur in streams where an actual fetch happened, not in ones
// where a cached copy of the entity to be fetched was used.

data class Fetch(
        override val id: Id,
        override val children: List<Id>,
        val url: String,
        val success: Boolean) : BazelContent