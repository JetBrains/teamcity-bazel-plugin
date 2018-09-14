package bazel.bazel.events

// Payload of an event reporting details of a given configuration.

data class Configuration(
        override val id: Id,
        override val children: List<Id>,
        val platformName: String,
        val mnemonic: String,
        val cpu: String,
        val makeVariableMap: Map<String, String>) : BazelContent