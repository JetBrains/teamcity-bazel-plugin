package jetbrains.buildServer.bazel

import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.BuildStartContextProcessor
import jetbrains.buildServer.serverSide.TeamCityProperties

class BazelBuildStartContextProcessor : BuildStartContextProcessor {
    override fun updateParameters(context: BuildStartContext) {
        val settingEnabled =
            TeamCityProperties
                .getBooleanOrTrue(BazelConstants.TEAMCITY_PROPERTY_REPORT_TARGET_LOG_TO_BUILD_LOG_SETTING_ENABLED)
                .toString()

        context.runnerContexts
            .filter { it.runType.type == BazelConstants.RUNNER_TYPE }
            .forEach {
                it.addRunnerParameter(
                    BazelConstants.PARAM_REPORT_TARGET_LOG_TO_BUILD_LOG_SETTING_ENABLED,
                    settingEnabled,
                )
            }
    }
}
