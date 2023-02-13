/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

class PathsServiceImpl(
        private val _buildStepContext: BuildStepContext,
        private val _buildAgentConfiguration: BuildAgentConfiguration,
        private val _buildAgentConfigurablePaths: BuildAgentConfigurablePaths,
        private val _pluginDescriptor: PluginDescriptor,
        private val _fileSystemService: FileSystemService,
        private val _environment: Environment,
        private val _parametersService: ParametersService) : PathsService {

    override val uniqueName: String
        get() = UUID.randomUUID().toString().replace("-", "")

    override fun getPath(pathType: PathType) = when (pathType) {
        PathType.WorkingDirectory -> _buildStepContext.runnerContext.workingDirectory
        PathType.Checkout -> _buildStepContext.runnerContext.build.checkoutDirectory
        PathType.AgentTemp -> _buildAgentConfigurablePaths.agentTempDirectory
        PathType.BuildTemp -> _buildAgentConfigurablePaths.buildTempDirectory
        PathType.GlobalTemp -> _buildAgentConfigurablePaths.cacheDirectory
        PathType.Plugins -> _buildAgentConfiguration.agentPluginsDirectory
        PathType.Plugin -> _pluginDescriptor.pluginRoot
        PathType.Tools -> _buildAgentConfiguration.agentToolsDirectory
        PathType.Lib -> _buildAgentConfiguration.agentLibDirectory
        PathType.Work -> _buildAgentConfiguration.workDirectory
        PathType.System -> _buildAgentConfiguration.systemDirectory
        PathType.Bin -> File(_buildAgentConfiguration.agentHomeDirectory, "bin")
        PathType.Config -> _buildAgentConfigurablePaths.agentConfDirectory
        PathType.Log -> _buildAgentConfigurablePaths.agentLogsDirectory
    }

    override val toolPath: File
        get() {
            var toolPathParam = _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH)
            if (toolPathParam == null) {
                return File(_buildStepContext.runnerContext.getToolPath(BazelConstants.BAZEL_CONFIG_NAME))
            }

            var toolPath = File(toolPathParam)
            if (!_fileSystemService.isAbsolute(toolPath)) {
                toolPath = File(getPath(PathType.Checkout), toolPathParam)
            }

            if (_fileSystemService.isDirectory(toolPath)) {
                if (_environment.osType == OSType.WINDOWS) {
                    toolPath = File(toolPath, "bazel.exe")
                } else {
                    toolPath = File(toolPath, "bazel")
                }
            }

            if (!_fileSystemService.isExists(toolPath)) {
                throw RunBuildException("Cannot find Bazel at \"$toolPathParam\".")
            }

            return toolPath;
        }
}