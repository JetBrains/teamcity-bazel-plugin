/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.*

/**
 * Bazel runner service.
 */
class BazelRunnerBuildService(
        buildStepContext: BuildStepContext,
        private val _commandRegistry: CommandRegistry,
        private val _commandFactory: BazelCommandFactory) : BuildServiceAdapter() {

    init {
        initialize(buildStepContext.runnerContext.build, buildStepContext.runnerContext)
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val parameters = runnerParameters

        val commandName = parameters[BazelConstants.PARAM_COMMAND]?.trim()
        if (commandName == null || commandName.isEmpty()) {
            val buildException = RunBuildException("Bazel command name is empty")
            buildException.isLogStacktrace = false
            throw buildException
        }

        val command = _commandFactory.createCommand(commandName)
        _commandRegistry.register(command)

        return command.commandLineBuilder.build(command)
    }

    override fun isCommandLineLoggingEnabled() = false
}
