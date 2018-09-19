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

    const val PARAM_COMMAND = "bazel-command"
    const val PARAM_BUILD_TARGET = "bazel-build-target"
    const val PARAM_CLEAN_TARGET = "bazel-clean-target"
    const val PARAM_TEST_TARGET = "bazel-test-target"
    const val PARAM_RUN_TARGET = "bazel-run-target"
    const val PARAM_WORKING_DIR = AgentRuntimeProperties.BUILD_WORKING_DIR

    val BUILD_FILE_NAME = Regex("BUILD(\\.bazel)?")
    const val WORKSPACE_FILE_NAME = "WORKSPACE"
}
