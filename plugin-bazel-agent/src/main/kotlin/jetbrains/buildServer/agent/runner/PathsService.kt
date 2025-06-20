

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.impl.config.BuildAgentConfigurablePaths
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor
import jetbrains.buildServer.bazel.BazelConstants
import jetbrains.buildServer.util.OSType
import java.io.File
import java.util.*

enum class PathType {
    Checkout,
    AgentTemp,
    Plugin,
    System,
}

class PathsService(
    private val _buildStepContext: BuildStepContext,
    private val _buildAgentConfiguration: BuildAgentConfiguration,
    private val _buildAgentConfigurablePaths: BuildAgentConfigurablePaths,
    private val _pluginDescriptor: PluginDescriptor,
    private val _fileSystemService: FileSystemService,
    private val _environment: Environment,
    private val _parametersService: ParametersService,
) {
    val uniqueName: String
        get() = UUID.randomUUID().toString().replace("-", "")

    fun getPath(pathType: PathType) =
        when (pathType) {
            PathType.Checkout -> _buildStepContext.runnerContext.build.checkoutDirectory
            PathType.AgentTemp -> _buildAgentConfigurablePaths.agentTempDirectory
            PathType.Plugin -> _pluginDescriptor.pluginRoot
            PathType.System -> _buildAgentConfiguration.systemDirectory
        }

    val toolPath: File
        get() {
            val toolPathParam = _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH)
            if (toolPathParam == null) {
                return File(_buildStepContext.runnerContext.getToolPath(BazelConstants.BAZEL_CONFIG_NAME))
            }

            var toolPath = File(toolPathParam)
            if (!_fileSystemService.isAbsolute(toolPath)) {
                toolPath = File(getPath(PathType.Checkout), toolPathParam)
            }

            if (_fileSystemService.isDirectory(toolPath)) {
                toolPath =
                    if (_environment.osType == OSType.WINDOWS) {
                        File(toolPath, "bazel.exe")
                    } else {
                        File(toolPath, "bazel")
                    }
            }

            if (!_fileSystemService.isExists(toolPath)) {
                throw RunBuildException("Cannot find Bazel at \"$toolPathParam\".")
            }

            return toolPath
        }
}
