

package jetbrains.buildServer.bazel

/**
 * Provides parameters for bazel runner.
 */
class BazelParametersProvider {

    val workingDirKey: String
        get() = BazelConstants.PARAM_WORKING_DIR

    val commandKey: String
        get() = BazelConstants.PARAM_COMMAND

    val targetsKey: String
        get() = BazelConstants.PARAM_TARGETS

    val toolPathKey: String
        get() = BazelConstants.TOOL_PATH

    val argumentsKey: String
        get() = BazelConstants.PARAM_ARGUMENTS

    val startupOptionsKey: String
        get() = BazelConstants.PARAM_STARTUP_OPTIONS

    val verbosityKey: String
        get() = BazelConstants.PARAM_VERBOSITY

    val verbosityValues: List<Verbosity>
        get() = Verbosity.values().toList()

    val integrationModeKey: String
        get() = BazelConstants.PARAM_INTEGRATION_MODE

    val integrationModes: List<IntegrationMode>
        get() = IntegrationMode.values().toList()

    // Build feature
    val remoteCacheKey: String
        get() = BazelConstants.PARAM_REMOTE_CACHE
}