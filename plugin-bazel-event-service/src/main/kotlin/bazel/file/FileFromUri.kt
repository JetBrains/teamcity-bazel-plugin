package bazel.file

import java.io.InputStream
import java.net.URI

class FileFromUri(
    override val name: String,
    val uri: String,
) : File {
    override fun createStream(): InputStream =
        when {
            uri.startsWith("bytestream") -> BytestreamReader.getInputStream(URI(uri))

            else -> URI(uri).toURL().openStream()
        }

    override fun toString() = "$name ($uri)"
}
