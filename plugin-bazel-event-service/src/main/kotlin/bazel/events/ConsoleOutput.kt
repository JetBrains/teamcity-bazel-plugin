package bazel.events

// Textual output written to standard output or standard error.

data class ConsoleOutput(
        override val streamId: StreamId,
        override val sequenceNumber: Long,
        override val eventTime: Timestamp,
        // The output stream type.
        val consoleOutputStream: ConsoleOutputStream,
        // The output stream content.
        val text: String)
    : OrderedBuildEvent