

package bazel

import java.io.File
import java.io.OutputStream

interface FileSystemService {
    fun write(file: File, writer: (OutputStream) -> Unit)

    fun generateTempFile(prefix: String, extension: String): File
}