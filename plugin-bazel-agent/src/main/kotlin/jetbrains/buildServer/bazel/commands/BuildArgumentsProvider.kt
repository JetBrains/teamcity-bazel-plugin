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
 * Provides arguments to bazel build command.
 */
class BuildArgumentsProvider : ArgumentsProvider {

    override fun getArguments(runnerContext: BuildRunnerContext): List<String> {
        val parameters = runnerContext.runnerParameters
        val arguments = ArrayList<String>()
        arguments.add(BazelConstants.COMMAND_BUILD)

        val targetValue = parameters[BazelConstants.PARAM_BUILD_TARGET]
        if (!targetValue.isNullOrBlank()) {
            arguments.add(targetValue!!.trim())
        }

        return arguments
    }
}
