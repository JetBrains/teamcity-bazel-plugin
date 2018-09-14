package bazel.bazel.events

// Payload of an event summarizing the progress of the build so far. Those
// events are also used to be parents of events where the more logical parent
// event cannot be posted yet as the needed information is not yet complete.

data class Progress(
        override val id: Id,
        override val children: List<Id>,
        // The next chunk of stdout that bazel produced since the last progress event
        // or the beginning of the build.
        val stdout: String,
        // The next chunk of stderr that bazel produced since the last progress event
        // or the beginning of the build.
        val stderr: String) : BazelContent