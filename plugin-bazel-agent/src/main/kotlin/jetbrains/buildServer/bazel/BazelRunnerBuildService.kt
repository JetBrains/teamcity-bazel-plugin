/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.runner.JavaRunnerConstants
import jetbrains.buildServer.util.StringUtil
import java.io.File
import kotlin.coroutines.experimental.buildSequence

/**
 * Bazel runner service.
 */
class BazelRunnerBuildService(
        buildStepContext: BuildStepContext,
        bazelCommands: List<BazelCommand>,
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _argumentsSplitter: BazelArgumentsSplitter,
        private val _commandRegistry: CommandRegistry) : BuildServiceAdapter() {

    private val _bazelCommands = bazelCommands.associate { it.command to it }

    init {
        initialize(buildStepContext.runnerContext.build, buildStepContext.runnerContext)
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val parameters = runnerParameters

        val commandName = parameters[BazelConstants.PARAM_COMMAND]
        if (StringUtil.isEmpty(commandName)) {
            val buildException = RunBuildException("Bazel command name is empty")
            buildException.isLogStacktrace = false
            throw buildException
        }

        val command = _bazelCommands[commandName]
        if (command == null) {
            val buildException = RunBuildException("Unable to construct arguments for bazel command $commandName")
            buildException.isLogStacktrace = false
            throw buildException
        }

        _commandRegistry.register(command)

        val sb = StringBuilder()
        sb.appendln(getPath(BazelConstants.BAZEL_CONFIG_NAME))
        for (arg in getArgs(command)) {
            sb.appendln(arg)
        }

        val bazelCommandFile = File(_pathsService.getPath(PathType.BuildTemp), _pathsService.uniqueName)
        bazelCommandFile.writeText(sb.toString())

        // get java executable
        val explicitJavaHome: String = _parametersService.tryGetParameter(ParameterType.Runner, JavaRunnerConstants.TARGET_JDK_HOME) ?: ""
        val propsAndVars = environmentVariables + systemProperties
        val baseDir = _pathsService.getPath(PathType.Checkout).absolutePath
        val javaHome = JavaRunnerUtil.findJavaHome(explicitJavaHome, propsAndVars, baseDir) ?: throw RunBuildException("Unable to find Java Home")
        val javaExecutable = JavaRunnerUtil.findJavaExecutablePath(javaHome, propsAndVars, baseDir) ?: throw RunBuildException("Unable to find Java")

        // get tool jar
        val pluginDir = _pathsService.getPath(PathType.Plugin)
        val jarFile = File(File(pluginDir, "tools"), "plugin-bazel-event-service.jar")

        val besArgs = mutableListOf<String>("-jar", jarFile.absolutePath, "-c=${bazelCommandFile.absolutePath}")
        _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_VERBOSITY)?.trim()?.let {
            Verbosity.tryParse(it)?.let {
                besArgs.add("-l=${it.id}")
            }
        }

        return createProgramCommandline(javaExecutable.absolutePath, besArgs)
    }

    private fun getArgs(command: BazelCommand): Sequence<String> = buildSequence {
        yield(command.command)
        yieldAll(command.arguments)
        _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_ARGUMENTS)?.trim()?.let {
            yieldAll(_argumentsSplitter.splitArguments(it))
        }
        _parametersService.tryGetParameter(ParameterType.System, "teamcity.buildType.id")?.let {
            if (!it.isBlank()) {
                yield("--project_id=$it")
            }
        }
    }

    private fun getPath(toolName: String): String {
        try {
            return getToolPath(toolName)
        } catch (e: ToolCannotBeFoundException) {
            val buildException = RunBuildException(e)
            buildException.isLogStacktrace = false
            throw buildException
        }
    }

    override fun isCommandLineLoggingEnabled() = false
}
