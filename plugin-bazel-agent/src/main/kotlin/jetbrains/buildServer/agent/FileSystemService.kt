

package jetbrains.buildServer.agent

import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface FileSystemService {
    fun isExists(file: File): Boolean

    fun isDirectory(file: File): Boolean

    fun isAbsolute(file: File): Boolean

    fun createDirectory(path: File): Boolean

    fun write(
        file: File,
        writer: (OutputStream) -> Unit,
    )

    fun read(
        file: File,
        reader: (InputStream) -> Unit,
    )

    fun remove(file: File)

    fun list(file: File): Sequence<File>
}
