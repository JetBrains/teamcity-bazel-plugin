package bazel.file

import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

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

    private class BytestreamURLStreamHandler : URLStreamHandler() {
        override fun openConnection(url: URL): URLConnection = BytestreamURLConnection(url)
    }
}
