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

package jetbrains.buildServer.bazel

import com.github.zafarkhaja.semver.Version
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.StringUtil
import java.io.File

/**
 * Determines bazel tool location.
 */
class BazelToolProvider(
        toolsRegistry: ToolProvidersRegistry,
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _environment: Environment,
        private val _fileSystemService: FileSystemService,
        private val _commandLineExecutor: CommandLineExecutor)
    : AgentLifeCycleAdapter(), ToolProvider {

    private var _lastVersion: Pair<File, Version>? = null

    init {
        toolsRegistry.registerToolProvider(this)
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.info("Locating ${BazelConstants.BAZEL_CONFIG_NAME} tool")
        _lastVersion = findVersions().sortedByDescending { it.second.buildMetadata }.firstOrNull()
        if (_lastVersion != null) {
            val version = _lastVersion!!
            LOG.info("Found ${BazelConstants.BAZEL_CONFIG_NAME} at ${version.first}")
            agent.configuration.apply {
                addConfigurationParameter(BazelConstants.BAZEL_CONFIG_PATH, version.first.canonicalPath)
                addConfigurationParameter(BazelConstants.BAZEL_CONFIG_NAME, version.second.toString())
            }
        } else {
            LOG.warn(unableToLocateToolErrorMessage)
        }
    }

    override fun supports(toolName: String): Boolean = BazelConstants.BAZEL_CONFIG_NAME.equals(toolName, true)

    override fun getPath(toolName: String): String {
        if (!supports(toolName)) throw ToolCannotBeFoundException("Unsupported tool $toolName")
        return _lastVersion?.first?.canonicalPath ?: throw ToolCannotBeFoundException(unableToLocateToolErrorMessage)
    }

    override fun getPath(toolName: String, build: AgentRunningBuild, runner: BuildRunnerContext): String =
            if (runner.isVirtualContext) BazelConstants.EXECUTABLE else build.agentConfiguration.configurationParameters[BazelConstants.BAZEL_CONFIG_PATH] ?: getPath(toolName)

    fun findVersions(): Sequence<Pair<File, Version>> =
            StringUtil.splitHonorQuotes(_environment.tryGetEnvironmentVariable("PATH") ?: "", File.pathSeparatorChar)
                    .asSequence()
                    .filter { it.isNotBlank() }
                    .map { File(it) }
                    .filter { _fileSystemService.isDirectory(it) }
                    .flatMap { _fileSystemService.list(it) }
                    .filter { !_fileSystemService.isDirectory(it) && PATH_PATTERN.matches(it.name) }
                    .map { Pair(it, _commandLineExecutor.tryExecute(SimpleProgramCommandLine(_environment.EnvironmentVariables.toMutableMap(), ".", it.canonicalPath, listOf("version")))) }
                    .filter { it.second.exitCode == 0 && it.second.stdOut.isNotBlank() }
                    .map { result -> Pair(result.first, result.second.stdOut.lines().map { tryParseVersion(it) }.firstOrNull { it != null }) }
                    .filter { it.second != null }
                    .map { Pair(it.first, it.second!!) }


    fun tryParseVersion(text: String): Version? =
            VERSION_PATTERN.find(text)?.groupValues?.get(1)?.let {
                try {
                    Version.valueOf(it)
                } catch (e: Throwable) {
                    LOG.warnAndDebugDetails("Failed to parse ${BazelConstants.BAZEL_CONFIG_NAME} version from line \"$it\": ${e.message}", e)
                    null
                }
            }

    companion object {
        private val LOG = Logger.getInstance(BazelToolProvider::class.java.name)
        private val VERSION_PATTERN = Regex("^Build label:\\s*(\\d+\\.\\d+\\.\\d).*$", RegexOption.IGNORE_CASE)
        private val PATH_PATTERN = Regex("^.*${BazelConstants.EXECUTABLE}(\\.(exe))?$", RegexOption.IGNORE_CASE)
        private const val unableToLocateToolErrorMessage = "Unable to locate tool ${BazelConstants.EXECUTABLE} in system. Please make sure to add it in the PATH variable."
    }
}
