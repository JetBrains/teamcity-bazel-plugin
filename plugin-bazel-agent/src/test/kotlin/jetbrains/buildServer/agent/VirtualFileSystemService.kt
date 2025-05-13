

package jetbrains.buildServer.agent

import java.io.*

class VirtualFileSystemService : FileSystemService {
    private val directories: MutableMap<File, DirectoryInfo> = mutableMapOf()
    private val files: MutableMap<File, FileInfo> = mutableMapOf()

    override fun write(
        file: File,
        writer: (OutputStream) -> Unit,
    ) {
        addFile(file)
        writer(files[file]!!.outputStream)
    }

    override fun read(
        file: File,
        reader: (InputStream) -> Unit,
    ) {
        reader(files[file]!!.inputStream)
    }

    fun addDirectory(
        directory: File,
        attributes: Attributes = Attributes(),
    ): VirtualFileSystemService {
        directories[directory] = DirectoryInfo(attributes)
        var parent: File? = directory
        while (parent != null) {
            parent = parent.parentFile
            if (parent != null) {
                if (!directories.contains(parent)) {
                    directories[parent] = DirectoryInfo(attributes)
                }
            }
        }

        return this
    }

    fun addFile(
        file: File,
        attributes: Attributes = Attributes(),
    ): VirtualFileSystemService {
        val parent = file.parentFile
        if (parent != null) {
            addDirectory(parent)
        }

        if (!files.containsKey(file)) {
            files[file] = FileInfo(attributes)
        }

        return this
    }

    override fun isExists(file: File): Boolean = directories.contains(file) || files.contains(file)

    override fun isDirectory(file: File): Boolean = directories.contains(file)

    override fun isAbsolute(file: File): Boolean = directories[file]?.attributes?.isAbsolute ?: files[file]?.attributes?.isAbsolute ?: false

    override fun createDirectory(path: File): Boolean {
        addDirectory(path)
        return true
    }

    override fun remove(file: File) {
        val fileInfo = files[file]
        if (fileInfo != null) {
            val errorOnRemove = fileInfo.attributes.errorOnRemove
            if (errorOnRemove != null) {
                throw errorOnRemove
            }

            files.remove(file)
        }

        val dirInfo = directories[file]
        if (dirInfo != null) {
            val errorOnRemove = dirInfo.attributes.errorOnRemove
            if (errorOnRemove != null) {
                throw errorOnRemove
            }

            directories.remove(file)
        }
    }

    override fun list(file: File): Sequence<File> =
        directories.keys
            .asSequence()
            .plus(files.map { it.key })
            .filter { it.parentFile == file }

    private data class FileInfo(
        val attributes: Attributes,
    ) {
        val inputStream: InputStream
        val outputStream: OutputStream

        init {
            outputStream = PipedOutputStream()
            inputStream = PipedInputStream(outputStream)
        }
    }

    private data class DirectoryInfo(
        val attributes: Attributes,
    )

    class Attributes {
        var isAbsolute: Boolean = false
        var errorOnRemove: Exception? = null
    }

    companion object {
        fun absolute(isAbsolute: Boolean = true): Attributes {
            val attr = Attributes()
            attr.isAbsolute = isAbsolute
            return attr
        }

        fun errorOnRemove(errorOnRemove: Exception): Attributes {
            val attr = Attributes()
            attr.errorOnRemove = errorOnRemove
            return attr
        }
    }
}
