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

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class WorkingDirectoryProviderImpl(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService)
    : WorkingDirectoryProvider {
    override val workingDirectory: File
        get() = _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_WORKING_DIR)?.let {
            try {
                LOG.info("Getting the working directory \"$it\" from the runner parameter \"${BazelConstants.PARAM_WORKING_DIR}\"")
                return File(it).absoluteFile
            } catch (ex: Throwable) {
                throw RunBuildException("Invalid working directory", ex)
            }
        } ?: _pathsService.getPath(PathType.Checkout).absoluteFile

    companion object {
        private val LOG = Logger.getInstance(WorkingDirectoryProviderImpl::class.java.name)
    }
}