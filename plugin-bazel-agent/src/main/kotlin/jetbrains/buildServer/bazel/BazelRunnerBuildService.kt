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
import jetbrains.buildServer.util.StringUtil
import kotlin.coroutines.experimental.buildSequence

/**
 * Bazel runner service.
 */
class BazelRunnerBuildService(
        buildStepContext: BuildStepContext,
        bazelCommands: List<BazelCommand>,
        private val _parametersService: ParametersService) : BuildServiceAdapter() {

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

        val toolPath = getPath(BazelConstants.BAZEL_CONFIG_NAME)
        return createProgramCommandline(toolPath, getArgs(command).toList())
    }

    private fun getArgs(command: BazelCommand): Sequence<String> = buildSequence {
        yield(command.command)
        yieldAll(command.arguments)
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

    override fun isCommandLineLoggingEnabled() = true
}
