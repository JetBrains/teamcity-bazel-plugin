

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.ProgramCommandLine
import java.io.File

class Workspace(
    val path: File,
    val cleanCommandLine: ProgramCommandLine,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Workspace

        return path == other.path
    }

    override fun hashCode(): Int = path.hashCode()

    override fun toString(): String = "Workspace(path=$path)"
}
