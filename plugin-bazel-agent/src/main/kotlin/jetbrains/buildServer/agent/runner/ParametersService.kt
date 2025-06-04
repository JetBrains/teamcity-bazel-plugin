

package jetbrains.buildServer.agent.runner

enum class ParameterType {
    Runner,
    Configuration,
    Environment,
    System,
}

interface ParametersService {
    fun tryGetParameter(
        parameterType: ParameterType,
        parameterName: String,
    ): String?

    fun getParameterNames(parameterType: ParameterType): Sequence<String>

    fun tryGetBuildFeatureParameter(
        buildFeatureType: String,
        parameterName: String,
    ): String?
}
