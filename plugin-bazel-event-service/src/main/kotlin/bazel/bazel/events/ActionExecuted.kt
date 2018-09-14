package bazel.bazel.events

// Payload of the event indicating the completion of an action. The main purpose
// of posting those events is to provide details on the root cause for a target
// failing; however, consumers of the build-event protocol must not assume
// that only failed actions are posted.

data class ActionExecuted(
        override val id: Id,
        override val children: List<Id>,
        // The mnemonic of the action that was executed
        val type: String,
        // The command-line of the action, if the action is a command.
        val cmdLines: List<String>,
        val success: Boolean,
        // Primary output; only provided for successful actions.
        val primaryOutput: File,
        // Location where to find the standard output of the action (e.g., a file path).
        val stdout: File,
        // Location where to find the standard error of the action (e.g., a file path).
        val stderr: File,
        // The exit code of the action, if it is available.
        val exitCode: Int): BazelContent