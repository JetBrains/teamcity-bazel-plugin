

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class ParametersServiceStub : ParametersService {
    private val params = mutableMapOf<Key, String>()
    private val featureParams = mutableMapOf<String, MutableMap<String, String>>()

    override fun tryGetParameter(
        parameterType: ParameterType,
        parameterName: String,
    ): String? = params[Key(parameterType, parameterName)]

    override fun getParameterNames(parameterType: ParameterType): Sequence<String> =
        params.filter { it.key.parameterType == parameterType }.map { it.key.parameterName }.asSequence()

    override fun tryGetBuildFeatureParameter(
        buildFeatureType: String,
        parameterName: String,
    ): String? = featureParams[buildFeatureType]?.get(parameterName)

    fun add(
        parameterType: ParameterType,
        parameterName: String,
        parameterValue: String,
    ): ParametersServiceStub {
        params[Key(parameterType, parameterName)] = parameterValue
        return this
    }

    fun add(
        buildFeatureType: String,
        parameterName: String,
        parameterValue: String,
    ): ParametersServiceStub {
        featureParams.getOrPut(buildFeatureType) { mutableMapOf() }[parameterName] = parameterValue
        return this
    }

    private data class Key(
        val parameterType: ParameterType,
        val parameterName: String,
    )
}
