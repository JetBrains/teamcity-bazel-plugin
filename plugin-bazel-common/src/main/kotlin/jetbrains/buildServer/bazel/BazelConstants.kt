

package jetbrains.buildServer.bazel

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
    const val PARAM_WORKING_DIR = "teamcity.build.workingDir" // includes path to build working dir
    const val PARAM_VERBOSITY = "verbosity"
    const val PARAM_INTEGRATION_MODE = "integration"
    const val PARAM_SUCCESS_WHEN_NO_TESTS = "successWhenNoTests"

    // build feature
    const val PARAM_REMOTE_CACHE = "remoteHttpCache"

    val BUILD_FILE_NAME = Regex("BUILD(\\.bazel)?")
    const val WORKSPACE_FILE_NAME = "WORKSPACE"
}
