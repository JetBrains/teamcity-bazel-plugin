

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class ParametersServiceStub : ParametersService {
    private val _params = mutableMapOf<Key, String>()
    private val _featureParams = mutableMapOf<String, MutableMap<String, String>>()

    override fun tryGetParameter(parameterType: ParameterType, parameterName: String): String? =
            _params[Key(parameterType, parameterName)]

    override fun getParameterNames(parameterType: ParameterType): Sequence<String> =
            _params.filter { it.key.parameterType == parameterType }.map { it.key.parameterName }.asSequence()

    override fun tryGetBuildFeatureParameter(buildFeatureType: String, parameterName: String): String? =
            _featureParams[buildFeatureType]?.get(parameterName)

    fun add(parameterType: ParameterType, parameterName: String, parameterValue: String): ParametersServiceStub {
        _params[Key(parameterType, parameterName)] = parameterValue
        return this
    }

    fun add(buildFeatureType: String, parameterName: String, parameterValue: String): ParametersServiceStub {
        _featureParams.getOrPut(buildFeatureType) { mutableMapOf() }[parameterName] = parameterValue
        return this
    }

    private data class Key(val parameterType: ParameterType, val parameterName: String)
}