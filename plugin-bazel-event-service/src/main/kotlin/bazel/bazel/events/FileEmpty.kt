

package bazel.bazel.events

import java.io.ByteArrayInputStream
import java.io.InputStream

class FileEmpty(
    override val name: String,
) : File {
    override fun createStream(): InputStream = ByteArrayInputStream(ByteArray(0))

    override fun toString() = "$name (empty)"
}
