/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.bazel.commands.CommandType
import jetbrains.buildServer.bazel.commands.bazel.*

/**
 * Provides parameters for bazel runner.
 */
class BazelParametersProvider {

    val types: List<CommandType> = listOf(
            BuildCommandType(),
            CleanCommandType(),
            RunCommandType(),
            TestCommandType())

    val workingDirKey: String
        get() = BazelConstants.PARAM_WORKING_DIR

    val commandKey: String
        get() = BazelConstants.PARAM_COMMAND

    val buildTargetKey: String
        get() = BazelConstants.PARAM_BUILD_TARGET

    val cleanTargetKey: String
        get() = BazelConstants.PARAM_CLEAN_TARGET

    val testTargetKey: String
        get() = BazelConstants.PARAM_TEST_TARGET

    val runTargetKey: String
        get() = BazelConstants.PARAM_RUN_TARGET

    val argumentsKey: String
        get() = BazelConstants.PARAM_ARGUMENTS
}