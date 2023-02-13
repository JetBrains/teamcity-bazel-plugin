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

import jetbrains.buildServer.agent.runner.*

class BazelCommandLineBuilder(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _workingDirectoryProvider: WorkingDirectoryProvider,
        private val _argumentsConverter: ArgumentsConverter)
    : CommandLineBuilder {
    override fun build(command: BazelCommand): ProgramCommandLine {
        // get java executable
        val environmentVariables = _parametersService.getParameterNames(ParameterType.Environment).associate { it to _parametersService.tryGetParameter(ParameterType.Environment, it) }.toMutableMap()
        environmentVariables.getOrPut("HOME") { System.getProperty("user.home") }

        return SimpleProgramCommandLine(
                environmentVariables,
                _workingDirectoryProvider.workingDirectory.absolutePath,
                _pathsService.toolPath.absolutePath,
                _argumentsConverter.convert(command.arguments).toList())
    }
}