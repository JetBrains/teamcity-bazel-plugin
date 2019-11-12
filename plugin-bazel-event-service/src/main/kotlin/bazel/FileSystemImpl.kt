package bazel

import java.io.File

class FileSystemImpl : FileSystem {
    override fun exists(file: File): Boolean = file.exists()

    override fun readFile(file: File): Sequence<String> =
            sequence {
                file.bufferedReader().use {
                    it.lineSequence()
                }
            }
}