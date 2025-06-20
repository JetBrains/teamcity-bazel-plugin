package bazel.file

import java.io.ByteArrayInputStream
import java.io.InputStream

class FileInMemory(
    override val name: String,
    private val _content: ByteArray,
) : File {
    override fun createStream(): InputStream = ByteArrayInputStream(_content)

    override fun toString() = "$name (${_content.size} bytes)"
}
