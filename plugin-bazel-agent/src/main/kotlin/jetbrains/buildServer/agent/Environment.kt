package jetbrains.buildServer.agent

import jetbrains.buildServer.util.OSType

interface Environment {
    val osType: OSType

    val EnvironmentVariables: Map<String, String>

    fun tryGetEnvironmentVariable(name: String): String?
}