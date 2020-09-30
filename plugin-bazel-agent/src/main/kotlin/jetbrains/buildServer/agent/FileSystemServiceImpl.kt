package jetbrains.buildServer.agent

import java.io.*

class FileSystemServiceImpl : FileSystemService {
    override fun isExists(file: File): Boolean = file.exists()

    override fun isDirectory(file: File): Boolean = file.isDirectory

    override fun isAbsolute(file: File): Boolean = file.isAbsolute

    override fun createDirectory(path: File): Boolean = path.mkdir()

    override fun write(file: File, writer: (OutputStream) -> Unit) = FileOutputStream(file).use(writer)

    override fun read(file: File, reader: (InputStream) -> Unit) = FileInputStream(file).use(reader)

    override fun remove(file: File) {
        if (isDirectory(file)) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    override fun list(file: File): Sequence<File> = file.listFiles()?.asSequence() ?: emptySequence()
}