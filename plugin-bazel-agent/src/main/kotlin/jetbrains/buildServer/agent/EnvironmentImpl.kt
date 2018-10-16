package jetbrains.buildServer.agent

class EnvironmentImpl : Environment {
    override val EnvironmentVariables: Map<String, String> get() = System.getenv()

    override fun tryGetEnvironmentVariable(name: String): String? = System.getenv(name)
}