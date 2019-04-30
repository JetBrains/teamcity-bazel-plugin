package bazel.bazel.events

import java.net.URI

data class File(
        // identifier indicating the nature of the file (e.g., "stdout", "stderr")
        val name: String,
        // A location where the contents of the file can be found. The string is encoded according to RFC2396.
        val uri: String) {
    @Suppress("UNUSED_PARAMETER")
    constructor(
            // identifier indicating the nature of the file (e.g., "stdout", "stderr")
            name: String,
            // The contents of the file, if they are guaranteed to be short.
            content: ByteArray) : this(name, "")

    val path: java.io.File? get() =
        try {
            java.io.File(URI(uri))
        }
        catch(ex: Exception) {
            null;
        }

    companion object {
        val empty = File("", "")
    }
}