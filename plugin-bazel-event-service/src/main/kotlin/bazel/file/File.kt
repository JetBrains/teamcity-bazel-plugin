package bazel.file

import java.io.InputStream

interface File {
    val name: String

    fun createStream(): InputStream
}
