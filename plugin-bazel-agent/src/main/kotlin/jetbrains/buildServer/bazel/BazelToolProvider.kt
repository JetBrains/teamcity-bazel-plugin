/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import com.github.zafarkhaja.semver.Version
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.SimpleCommandLineProcessRunner
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.StringUtil

import java.io.File

/**
 * Determines bazel tool location.
 */
class BazelToolProvider(toolsRegistry: ToolProvidersRegistry,
                                 events: EventDispatcher<AgentLifeCycleListener>)
    : AgentLifeCycleAdapter(), ToolProvider {


    init {
        toolsRegistry.registerToolProvider(this)
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.info("Locating $CONFIG_NAME tool")
        findToolPath()?.let {
            LOG.info("Found $CONFIG_NAME at ${it.first}")
            agent.configuration.apply {
                addConfigurationParameter(CONFIG_PATH, it.first)
                addConfigurationParameter(CONFIG_NAME, it.second.toString())
            }
        }
    }

    override fun supports(toolName: String): Boolean {
        return CONFIG_NAME.equals(toolName, true)
    }

    override fun getPath(toolName: String): String {
        if (!supports(toolName)) throw ToolCannotBeFoundException("Unsupported tool $toolName")

        findToolPath()?.let {
            return it.first
        }

        throw ToolCannotBeFoundException("""
                Unable to locate tool $toolName in system. Please make sure to add it in the PATH variable
                """.trimIndent())
    }

    override fun getPath(toolName: String,
                         build: AgentRunningBuild,
                         runner: BuildRunnerContext) =
            build.agentConfiguration.configurationParameters[CONFIG_PATH] ?: getPath(toolName)

    /**
     * Returns a first matching file in the list of directories.
     *
     * @return first matching file.
     */
    private fun findToolPath(): Pair<String, Version>? {
        val paths = StringUtil.splitHonorQuotes(System.getenv("PATH"), File.pathSeparatorChar)

        return paths.mapNotNull { File(it).listFiles() }
                .flatMap { it.map { it.absolutePath } }
                .filter { PATH_PATTERN.matches(it) }
                .mapNotNull {
                    try {
                        val commandLine = getVersionCommandLine(it)
                        val result = SimpleCommandLineProcessRunner.runCommand(commandLine, byteArrayOf())
                        val version = VERSION_PATTERN.find(result.stdout)?.destructured?.component1() ?: result.stdout
                        it to Version.valueOf(version)
                    } catch (e: Throwable) {
                        LOG.warnAndDebugDetails("Failed to parse $CONFIG_NAME version: ${e.message}", e)
                        null
                    }
                }
                .sortedByDescending { it.second }
                .firstOrNull()
    }

    private fun getVersionCommandLine(toolPath: String): GeneralCommandLine {
        val commandLine = GeneralCommandLine()
        commandLine.exePath = toolPath
        commandLine.addParameter("version")
        return commandLine
    }

    companion object {
        private val LOG = Logger.getInstance(BazelToolProvider::class.java.name)
        private const val CONFIG_NAME = BazelConstants.BAZEL_CONFIG_NAME
        private const val CONFIG_PATH = BazelConstants.BAZEL_CONFIG_PATH
        private val VERSION_PATTERN = Regex("Build label:\\s([^\\s]+)", RegexOption.IGNORE_CASE)
        private val PATH_PATTERN = Regex("^.*$CONFIG_NAME(\\.(exe))?$", RegexOption.IGNORE_CASE)
    }
}
