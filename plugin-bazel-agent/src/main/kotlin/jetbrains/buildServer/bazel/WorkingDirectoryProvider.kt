

package jetbrains.buildServer.bazel

import java.io.File

interface WorkingDirectoryProvider {
    val workingDirectory: File
}