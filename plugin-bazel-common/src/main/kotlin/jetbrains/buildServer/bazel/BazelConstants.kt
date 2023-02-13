/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.AgentRuntimeProperties

/**
 * Bazel runner constants.
 */
object BazelConstants {
    const val RUNNER_TYPE = "bazel"
    const val RUNNER_DISPLAY_NAME = "Bazel"
    const val RUNNER_DESCRIPTION = "Provides Bazel build support"
    const val EXECUTABLE = "bazel"

    const val BUILD_FEATURE_TYPE = "BazelBuildFeature"
    const val BUILD_FEATURE_DISPLAY_NAME = "Bazel build settings"

    const val BAZEL_CONFIG_NAME = "$RUNNER_TYPE.version"
    const val BAZEL_CONFIG_PATH = "$RUNNER_TYPE.path"

    const val COMMAND_BUILD = "build"
    const val COMMAND_CLEAN = "clean"
    const val COMMAND_RUN = "run"
    const val COMMAND_TEST = "test"
    const val COMMAND_SHUTDOWN = "shutdown"

    const val TOOL_PATH = "toolPath"
    const val PARAM_ARGUMENTS = "arguments"
    const val PARAM_STARTUP_OPTIONS = "startupOptions"
    const val PARAM_COMMAND = "command"
    const val PARAM_TARGETS = "targets"
    const val PARAM_WORKING_DIR = AgentRuntimeProperties.BUILD_WORKING_DIR
    const val PARAM_VERBOSITY = "verbosity"
    const val PARAM_INTEGRATION_MODE = "integration"

    // build feature
    const val PARAM_REMOTE_CACHE = "remoteHttpCache"

    val BUILD_FILE_NAME = Regex("BUILD(\\.bazel)?")
    const val WORKSPACE_FILE_NAME = "WORKSPACE"
}
