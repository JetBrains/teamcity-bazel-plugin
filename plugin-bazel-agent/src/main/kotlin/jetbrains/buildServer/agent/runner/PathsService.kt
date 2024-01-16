

package jetbrains.buildServer.agent.runner

import java.io.File

interface PathsService {
    val uniqueName: String

    val toolPath: File

    fun getPath(pathType: PathType): File
}