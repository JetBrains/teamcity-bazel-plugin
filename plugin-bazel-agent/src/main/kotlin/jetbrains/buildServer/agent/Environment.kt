package jetbrains.buildServer.agent

interface Environment {
    val EnvironmentVariables: Map<String, String>

    fun tryGetEnvironmentVariable(name: String): String?
}