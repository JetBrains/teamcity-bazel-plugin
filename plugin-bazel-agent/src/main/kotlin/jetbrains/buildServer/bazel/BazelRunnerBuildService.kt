/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.runner.BuildServiceAdapter
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.bazel.commands.BuildArgumentsProvider
import jetbrains.buildServer.bazel.commands.CleanArgumentsProvider
import jetbrains.buildServer.bazel.commands.RunArgumentsProvider
import jetbrains.buildServer.bazel.commands.TestArgumentsProvider
import jetbrains.buildServer.util.StringUtil

/**
 * Bazel runner service.
 */
class BazelRunnerBuildService : BuildServiceAdapter() {

    private val myArgumentsProviders = mapOf(
            Pair(BazelConstants.COMMAND_BUILD, BuildArgumentsProvider()),
            Pair(BazelConstants.COMMAND_CLEAN, CleanArgumentsProvider()),
            Pair(BazelConstants.COMMAND_RUN, RunArgumentsProvider()),
            Pair(BazelConstants.COMMAND_TEST, TestArgumentsProvider()))

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val parameters = runnerParameters

        val commandName = parameters[BazelConstants.PARAM_COMMAND]
        if (StringUtil.isEmpty(commandName)) {
            val buildException = RunBuildException("Bazel command name is empty")
            buildException.isLogStacktrace = false
            throw buildException
        }

        val argumentsProvider = myArgumentsProviders[commandName]
        if (argumentsProvider == null) {
            val buildException = RunBuildException("Unable to construct arguments for bazel command $commandName")
            buildException.isLogStacktrace = false
            throw buildException
        }

        val toolPath = getPath(BazelConstants.BAZEL_CONFIG_NAME)
        val arguments = argumentsProvider.getArguments(runnerContext)

        return createProgramCommandline(toolPath, arguments)
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
