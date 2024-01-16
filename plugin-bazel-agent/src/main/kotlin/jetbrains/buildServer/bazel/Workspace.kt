

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.ProgramCommandLine
import java.io.File

class Workspace(val path: File, val cleanCommandLine: ProgramCommandLine) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Workspace

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun toString(): String {
        return "Workspace(path=$path)"
    }
}