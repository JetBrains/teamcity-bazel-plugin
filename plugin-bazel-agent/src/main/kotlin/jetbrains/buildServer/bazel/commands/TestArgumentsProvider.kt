/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.bazel.ArgumentsProvider
import jetbrains.buildServer.bazel.BazelConstants

import java.util.ArrayList

/**
 * Provides arguments to bazel test command.
 */
class TestArgumentsProvider : ArgumentsProvider {

    override fun getArguments(runnerContext: BuildRunnerContext): List<String> {
        val parameters = runnerContext.runnerParameters
        val arguments = ArrayList<String>()
        arguments.add(BazelConstants.COMMAND_TEST)

        val targetValue = parameters[BazelConstants.PARAM_TEST_TARGET]
        if (!targetValue.isNullOrBlank()) {
            arguments.add(targetValue!!.trim())
        }

        return arguments
    }
}
