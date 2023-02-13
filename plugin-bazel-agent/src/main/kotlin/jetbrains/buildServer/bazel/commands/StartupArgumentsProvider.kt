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

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.bazel.*

class StartupArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsSplitter: BazelArgumentsSplitter) : ArgumentsProvider {
    override fun getArguments(command: BazelCommand): Sequence<CommandArgument> =
            sequence {
                _parametersService.tryGetBuildFeatureParameter(BazelConstants.BUILD_FEATURE_TYPE, BazelConstants.PARAM_STARTUP_OPTIONS)?.trim()?.let {
                    yieldAll(_argumentsSplitter.splitArguments(it).map { CommandArgument(CommandArgumentType.StartupOption, it) })
                }

                _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_STARTUP_OPTIONS)?.trim()?.let {
                    yieldAll(_argumentsSplitter.splitArguments(it).map { CommandArgument(CommandArgumentType.StartupOption, it) })
                }
            }
}