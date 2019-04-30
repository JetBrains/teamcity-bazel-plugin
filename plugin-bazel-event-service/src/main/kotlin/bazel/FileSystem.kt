package bazel

import java.io.File

interface FileSystem {
    fun exists(file: File): Boolean

    fun readFile(file: File): Sequence<String>
}