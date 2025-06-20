

package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.impl.OSTypeDetector
import jetbrains.buildServer.util.OSType

class Environment(
    private val _osTypeDetector: OSTypeDetector,
) {
    val osType: OSType get() = _osTypeDetector.detect()

    val environmentVariables: Map<String, String> get() = System.getenv()

    fun tryGetEnvironmentVariable(name: String): String? = System.getenv(name)
}
