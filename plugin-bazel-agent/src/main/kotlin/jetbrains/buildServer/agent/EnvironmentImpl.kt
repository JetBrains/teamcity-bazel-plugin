

package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.impl.OSTypeDetector
import jetbrains.buildServer.util.OSType

class EnvironmentImpl(
    private val _osTypeDetector: OSTypeDetector,
) : Environment {
    override val osType: OSType get() = _osTypeDetector.detect()

    override val environmentVariables: Map<String, String> get() = System.getenv()

    override fun tryGetEnvironmentVariable(name: String): String? = System.getenv(name)
}
