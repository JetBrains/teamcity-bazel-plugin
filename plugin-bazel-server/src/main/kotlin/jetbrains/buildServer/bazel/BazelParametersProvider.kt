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

    val buildTargetsKey: String
        get() = BazelConstants.PARAM_BUILD_TARGETS

    val cleanTargetsKey: String
        get() = BazelConstants.PARAM_CLEAN_TARGETS

    val testTargetsKey: String
        get() = BazelConstants.PARAM_TEST_TARGETS

    val runTargetsKey: String
        get() = BazelConstants.PARAM_RUN_TARGETS

    val argumentsKey: String
        get() = BazelConstants.PARAM_ARGUMENTS

    val startupOptionsKey: String
        get() = BazelConstants.PARAM_STARTUP_OPTIONS

    val verbosityKey: String
        get() = BazelConstants.PARAM_VERBOSITY

    val verbosityValues: List<Verbosity>
        get() = Verbosity.values().toList()
}