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
 * Provides arguments to bazel build command.
 */
class BuildCommand(
        override val commandLineBuilder: CommandLineBuilder,
        private val _parametersService: ParametersService,
        private val _commonArgumentsProvider: ArgumentsProvider,
        private val _argumentsSplitter: BazelArgumentsSplitter)
    : BazelCommand {

    override val command: String = BazelConstants.COMMAND_BUILD

    override val arguments: Sequence<CommandArgument>
        get() = buildSequence {
            yieldAll(_commonArgumentsProvider.getArguments(this@BuildCommand))
            _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_BUILD_TARGETS)?.let {
                if (!it.isBlank()) {
                    yieldAll(_argumentsSplitter.splitArguments(it).map { CommandArgument(CommandArgumentType.Target, it) })
                }
            }
        }
}
