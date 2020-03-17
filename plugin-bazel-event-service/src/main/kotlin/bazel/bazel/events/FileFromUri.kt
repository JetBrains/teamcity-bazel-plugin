package bazel.bazel.events

import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class FileFromUri(
        override val name: String,
        val uri: String)
    : File {
    override fun createStream(): InputStream {
        val connection: URLConnection = URL(uri).openConnection()
        connection.connect()
        return connection.getInputStream()
    }

    override fun toString() = "$name ($uri)"
}