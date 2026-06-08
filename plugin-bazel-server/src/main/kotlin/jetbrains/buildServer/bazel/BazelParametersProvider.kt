

package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.TeamCityProperties

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

    val successWhenNoTestsKey: String
        get() = BazelConstants.PARAM_SUCCESS_WHEN_NO_TESTS

    val reportTargetLogToBuildLogKey: String
        get() = BazelConstants.PARAM_REPORT_TARGET_LOG_TO_BUILD_LOG

    val reportTargetLogToBuildLogSettingEnabledKey: String
        get() = BazelConstants.PARAM_REPORT_TARGET_LOG_TO_BUILD_LOG_SETTING_ENABLED

    val reportTargetLogToBuildLogSettingEnabled: Boolean
        get() = TeamCityProperties.getBooleanOrTrue(
            BazelConstants.TEAMCITY_PROPERTY_REPORT_TARGET_LOG_TO_BUILD_LOG_SETTING_ENABLED,
        )

    val integrationModes: List<IntegrationMode>
        get() = IntegrationMode.values().toList()

    // Build feature
    val remoteCacheKey: String
        get() = BazelConstants.PARAM_REMOTE_CACHE
}
