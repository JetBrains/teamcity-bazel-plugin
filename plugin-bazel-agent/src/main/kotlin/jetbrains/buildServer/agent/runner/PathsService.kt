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
    private val _fileSystem: FileSystemService,
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

    val toolPath: String
        get() {
            val toolPathParam = _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH)
            return when (toolPathParam) {
                null -> _buildStepContext.runnerContext.getToolPath(BazelConstants.BAZEL_CONFIG_NAME)

                // force search inside PATH, tool might be installed during a previous build step
                "bazel", "bazel.exe" -> toolPathParam

                else ->
                    File(toolPathParam)
                        .let { if (_fileSystem.isAbsolute(it)) it else getPath(PathType.Checkout).resolve(it) }
                        .let {
                            if (_fileSystem.isDirectory(it)) {
                                it.resolve(if (_environment.osType == OSType.WINDOWS) "bazel.exe" else "bazel")
                            } else {
                                it
                            }
                        }.takeIf { _fileSystem.isExists(it) }
                        ?.path
                        ?: throw RunBuildException("Cannot find Bazel at \"$toolPathParam\".")
            }
        }
}
