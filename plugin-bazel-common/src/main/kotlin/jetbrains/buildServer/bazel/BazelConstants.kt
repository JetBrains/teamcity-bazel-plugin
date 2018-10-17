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
    const val EXECUTABLE = "bazel"

    const val BUILD_FEATURE_TYPE = "bazel-build-feature"
    const val BUILD_FEATURE_DISPLAY_NAME = "Bazel"

    const val BAZEL_CONFIG_NAME = "$RUNNER_TYPE.version"
    const val BAZEL_CONFIG_PATH = "$RUNNER_TYPE.path"

    const val COMMAND_BUILD = "build"
    const val COMMAND_CLEAN = "clean"
    const val COMMAND_RUN = "run"
    const val COMMAND_TEST = "test"
    const val COMMAND_SHUTDOWN = "shutdown"

    const val PARAM_ARGUMENTS = "arguments"
    const val PARAM_STARTUP_OPTIONS = "startupOptions"
    const val PARAM_COMMAND = "command"
    const val PARAM_TARGETS = "targets"
    const val PARAM_WORKING_DIR = AgentRuntimeProperties.BUILD_WORKING_DIR
    const val PARAM_VERBOSITY = "verbosity"

    // build feature
    const val PARAM_REMOTE_CACHE = "remote_http_cache"

    val BUILD_FILE_NAME = Regex("BUILD(\\.bazel)?")
    const val WORKSPACE_FILE_NAME = "WORKSPACE"
}
