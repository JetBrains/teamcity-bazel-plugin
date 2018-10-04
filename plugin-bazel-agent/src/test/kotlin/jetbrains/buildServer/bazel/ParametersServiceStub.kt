package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class ParametersServiceStub : ParametersService {
    private val _dict = mutableMapOf<Key, String>()

    override fun tryGetParameter(parameterType: ParameterType, parameterName: String): String? =
            _dict[Key(parameterType, parameterName)]

    override fun getParameterNames(parameterType: ParameterType): Sequence<String> =
            _dict.filter { it.key.parameterType == parameterType }.map { it.key.parameterName }.asSequence()

    fun add(parameterType: ParameterType, parameterName: String, parameterValue: String): ParametersServiceStub {
        _dict[Key(parameterType, parameterName)] = parameterValue
        return this
    }

    private data class Key(val parameterType: ParameterType, val parameterName: String)
}