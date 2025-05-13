

package bazel.bazel.events

import bazel.BytestreamURLStreamHandler
import java.io.InputStream
import java.net.URL
import java.net.URLConnection

class FileFromUri(
    override val name: String,
    val uri: String,
) : File {
    override fun createStream(): InputStream {
        val url =
            if (uri.startsWith("bytestream")) {
                URL(null, uri, BytestreamURLStreamHandler())
            } else {
                URL(uri)
            }
        val connection: URLConnection = url.openConnection()
        connection.connect()
        return connection.getInputStream()
    }

    override fun toString() = "$name ($uri)"
}
