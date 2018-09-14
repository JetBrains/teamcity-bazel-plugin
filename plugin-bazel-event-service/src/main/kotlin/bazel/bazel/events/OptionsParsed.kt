package bazel.bazel.events

// Payload of an event reporting on the parsed options, grouped in various ways.

data class OptionsParsed(
        override val id: Id,
        override val children: List<Id>,
        val cmdLines: List<String>,
        val explicitCmdLines: List<String>,
        val startupOptions: List<String>,
        val explicitStartupOptions: List<String>) : BazelContent