/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.bazel.BazelCommand
import jetbrains.buildServer.bazel.BazelConstants
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to bazel run command.
 */
class RunCommand(
        private val _parametersService: ParametersService)
    : BazelCommand {

    override val command: String = BazelConstants.COMMAND_RUN

    override val arguments: Sequence<String>
        get() = buildSequence {
            _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_RUN_TARGET)?.let {
                if (!it.isBlank()) {
                    yield(it)
                }
            }
        }
}
