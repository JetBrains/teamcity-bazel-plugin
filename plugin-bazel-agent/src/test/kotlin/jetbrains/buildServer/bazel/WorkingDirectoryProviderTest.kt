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
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class WorkingDirectoryProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathsService: PathsService
    private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathsService = _ctx.mock<PathsService>(PathsService::class.java)
        _parametersService = _ctx.mock<ParametersService>(ParametersService::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("wd", File("wd").absoluteFile),
                arrayOf(null, File("checkout").absoluteFile),
                arrayOf("", File("").absoluteFile)
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideWorkingDirectory(workingDir: String?, expectedWorkingDirectory: File) {
        // given
        val workingDirectoryProvider = WorkingDirectoryProviderImpl(_pathsService, _parametersService)
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_WORKING_DIR)
                will(returnValue(workingDir))

                allowing<PathsService>(_pathsService).getPath(PathType.Checkout)
                will(returnValue(File("checkout")))
            }
        })

        // when
        val actualWorkingDirectory = workingDirectoryProvider.workingDirectory

        // then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualWorkingDirectory, expectedWorkingDirectory)
    }
}