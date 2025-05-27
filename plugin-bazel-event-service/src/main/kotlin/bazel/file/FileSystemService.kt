package bazel.file

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class FileSystemService {
    fun write(
        file: File,
        writer: (OutputStream) -> Unit,
    ) = FileOutputStream(file).use(writer)

    fun generateTempFile(
        prefix: String,
        extension: String,
    ): File = File.createTempFile(prefix, extension)
}
