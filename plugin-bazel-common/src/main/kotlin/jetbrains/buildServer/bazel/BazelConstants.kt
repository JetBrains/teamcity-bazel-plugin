/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.AgentRuntimeProperties

/**
 * Bazel runner constants.
 */
object BazelConstants {
    const val RUNNER_TYPE = "bazel"
    const val RUNNER_DISPLAY_NAME = "bazel"
    const val RUNNER_DESCRIPTION = "Provides bazel build system support"
    const val BAZEL_CONFIG_NAME = "Bazel"
    const val BAZEL_CONFIG_PATH = BAZEL_CONFIG_NAME + "_Path"

    const val COMMAND_BUILD = "build"
    const val COMMAND_CLEAN = "clean"
    const val COMMAND_RUN = "run"
    const val COMMAND_TEST = "test"
    const val COMMAND_SHUTDOWN = "shutdown"

    const val PARAM_ARGUMENTS = "arguments"
    const val PARAM_STARTUP_OPTIONS = "startup-options"
    const val PARAM_COMMAND = "command"
    const val PARAM_TARGETS = "targets"
    const val PARAM_WORKING_DIR = AgentRuntimeProperties.BUILD_WORKING_DIR
    const val PARAM_VERBOSITY = "verbosity"

    val BUILD_FILE_NAME = Regex("BUILD(\\.bazel)?")
    const val WORKSPACE_FILE_NAME = "WORKSPACE"
}
