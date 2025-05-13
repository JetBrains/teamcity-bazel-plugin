

package bazel

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class FileSystemServiceImpl : FileSystemService {
    override fun write(
        file: File,
        writer: (OutputStream) -> Unit,
    ) = FileOutputStream(file).use(writer)

    override fun generateTempFile(
        prefix: String,
        extension: String,
    ) = File.createTempFile(prefix, extension)
}
