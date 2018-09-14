package bazel.bazel.events

data class File(
        // identifier indicating the nature of the file (e.g., "stdout", "stderr")
        val name: String,
        // A location where the contents of the file can be found. The string is encoded according to RFC2396.
        val uri: String) {
    constructor(
            // identifier indicating the nature of the file (e.g., "stdout", "stderr")
            name: String,
            // The contents of the file, if they are guaranteed to be short.
            content: ByteArray) : this(name, "")

    companion object {
        val empty = File("", "")
    }
}