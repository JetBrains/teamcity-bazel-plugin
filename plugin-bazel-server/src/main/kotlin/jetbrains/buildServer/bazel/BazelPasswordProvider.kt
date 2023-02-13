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

import jetbrains.buildServer.serverSide.Parameter
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SimpleParameter
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider
import java.net.MalformedURLException
import java.net.URL

class BazelPasswordProvider : PasswordsProvider {

    override fun getPasswordParameters(build: SBuild): MutableCollection<Parameter> {
        val parameters = mutableListOf<Parameter>()
        build.getBuildFeaturesOfType(BazelConstants.BUILD_FEATURE_TYPE).forEach { feature ->
            feature.parameters[BazelConstants.PARAM_REMOTE_CACHE]?.let { remoteCache ->
                try {
                    URL(remoteCache.trim())
                } catch (e: MalformedURLException) {
                    return@let
                }.userInfo?.let {
                    parameters.add(SimpleParameter(BazelConstants.PARAM_REMOTE_CACHE + "-user", it))
                }
            }
        }
        return parameters
    }
}