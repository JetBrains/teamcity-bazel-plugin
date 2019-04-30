package bazel

import java.io.File
import kotlin.coroutines.experimental.buildSequence

class FileSystemImpl : FileSystem {
    override fun exists(file: File): Boolean = file.exists()

    override fun readFile(file: File): Sequence<String> =
            buildSequence {
                file.bufferedReader().use {
                    it.lineSequence()
                }
            }
}