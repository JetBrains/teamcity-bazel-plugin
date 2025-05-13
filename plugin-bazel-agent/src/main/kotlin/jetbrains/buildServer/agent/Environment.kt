

package jetbrains.buildServer.agent

import jetbrains.buildServer.util.OSType

interface Environment {
    val osType: OSType

    val environmentVariables: Map<String, String>

    fun tryGetEnvironmentVariable(name: String): String?
}
