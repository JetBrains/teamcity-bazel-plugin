/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.bazel.*
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to bazel run command.
 */
class RunCommand(
        private val _parametersService: ParametersService,
        private val _argumentsSplitter: BazelArgumentsSplitter,
        override val commandLineBuilder: CommandLineBuilder,
        private val _commonArgumentsProvider: ArgumentsProvider,
        private val _targetsArgumentsProvider: ArgumentsProvider)
    : BazelCommand {

    override val command: String = BazelConstants.COMMAND_RUN

    override val arguments: Sequence<CommandArgument>
        get() = buildSequence {
            yield(CommandArgument(CommandArgumentType.Command, command))
            yieldAll(_commonArgumentsProvider.getArguments(this@RunCommand))
            yieldAll(_targetsArgumentsProvider.getArguments(this@RunCommand))
        }
}
