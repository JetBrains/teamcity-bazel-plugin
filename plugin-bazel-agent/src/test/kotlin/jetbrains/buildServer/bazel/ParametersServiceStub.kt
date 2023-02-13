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