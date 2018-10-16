/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
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

    private var _lastVersion: Pair<String, Version>? = null

    init {
        toolsRegistry.registerToolProvider(this)
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.info("Locating ${BazelConstants.BAZEL_CONFIG_NAME} tool")
        _lastVersion = findVersions().sortedByDescending { it.second }.firstOrNull()
        if (_lastVersion != null) {
            val version = _lastVersion!!
            LOG.info("Found ${BazelConstants.BAZEL_CONFIG_NAME} at ${version.first}")
            agent.configuration.apply {
                addConfigurationParameter(BazelConstants.BAZEL_CONFIG_PATH, version.first)
                addConfigurationParameter(BazelConstants.BAZEL_CONFIG_NAME, version.second.toString())
            }
        } else {
            LOG.warn(unableToLocateToolErrorMessage)
        }
    }

    override fun supports(toolName: String): Boolean = BazelConstants.BAZEL_CONFIG_NAME.equals(toolName, true)

    override fun getPath(toolName: String): String {
        if (!supports(toolName)) throw ToolCannotBeFoundException("Unsupported tool $toolName")
        return _lastVersion?.first ?: throw ToolCannotBeFoundException(unableToLocateToolErrorMessage)
    }

    override fun getPath(toolName: String, build: AgentRunningBuild, runner: BuildRunnerContext): String =
            if (runner.isVirtualContext) BazelConstants.EXECUTABLE else build.agentConfiguration.configurationParameters[BazelConstants.BAZEL_CONFIG_PATH] ?: getPath(toolName)

    private fun findVersions(): Sequence<Pair<String, Version>> =
            StringUtil.splitHonorQuotes(_environment.tryGetEnvironmentVariable("PATH") ?: "", File.pathSeparatorChar)
                    .asSequence()
                    .filter { it.isNotBlank() }
                    .flatMap { _fileSystemService.list(File(it)) }
                    .map { it.absolutePath }
                    .filter { PATH_PATTERN.matches(it) }
                    .map {
                        val result = _commandLineExecutor.tryExecute(SimpleProgramCommandLine(_environment.EnvironmentVariables.toMutableMap(), ".", it, listOf("version")))
                        var version: Version? = null
                        if (result.exitCode == 0 && result.stdOut.isNotBlank()) {
                            version = tryParseVersion(result.stdOut)
                        }

                        if (version == null) {
                            LOG.warn(result.stdErr)
                        }

                        Pair(it, version)
                    }
                    .filter { it.second != null }
                    .map { Pair(it.first, it.second!!) }


    private fun tryParseVersion(text: String): Version? =
            try {
                Version.valueOf(VERSION_PATTERN.find(text)?.destructured?.component1() ?: text)
            } catch (e: Throwable) {
                LOG.warnAndDebugDetails("Failed to parse ${BazelConstants.BAZEL_CONFIG_NAME} version: ${e.message}", e)
                null
            }

    companion object {
        private val LOG = Logger.getInstance(BazelToolProvider::class.java.name)
        private val VERSION_PATTERN = Regex("Build label:\\s([^\\s]+)", RegexOption.IGNORE_CASE)
        private val PATH_PATTERN = Regex("^.*${BazelConstants.EXECUTABLE}(\\.(exe))?$", RegexOption.IGNORE_CASE)
        private const val unableToLocateToolErrorMessage = "Unable to locate tool ${BazelConstants.EXECUTABLE} in system. Please make sure to add it in the PATH variable."
    }
}
