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

package jetbrains.buildServer.agent

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.impl.config.BuildAgentConfigurablePaths
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsServiceImpl
import jetbrains.buildServer.bazel.BazelConstants
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class PathsServiceTest {
    @MockK private lateinit var _buildStepContext: BuildStepContext
    @MockK private lateinit var _buildAgentConfiguration: BuildAgentConfiguration
    @MockK private lateinit var _buildAgentConfigurablePaths: BuildAgentConfigurablePaths
    @MockK private lateinit var _pluginDescriptor: PluginDescriptor
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideToolPathWhenToolPathWasNotDefined() {
        // Given
        val pathsService = createInstance()

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns null
        every { _buildStepContext.runnerContext } returns mockk<BuildRunnerContext>() {
            every { getToolPath(BazelConstants.BAZEL_CONFIG_NAME) } returns "default_bazel"
        }

        val actualToolPath = pathsService.toolPath

        // Then
        Assert.assertEquals(actualToolPath, File("default_bazel"))
    }

    @Test
    fun shouldProvideToolPathWhenCustomToolPathIsAbsoluteExecutable() {
        // Given
        val pathsService = createInstance()
        val path = File("custom_bazel")

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { _fileSystemService.isAbsolute(path) } returns true
        every { _fileSystemService.isDirectory(path) } returns false
        every { _fileSystemService.isExists(path) } returns true

        val actualToolPath = pathsService.toolPath

        // Then
        Assert.assertEquals(actualToolPath, path)
    }

    @Test
    fun shouldThrowExceptionWhenExecutableDoesNotExist() {
        // Given
        val pathsService = createInstance()
        val path = File("custom_bazel")

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { _fileSystemService.isAbsolute(path) } returns true
        every { _fileSystemService.isDirectory(path) } returns false
        every { _fileSystemService.isExists(path) } returns false

        // Then
        try {
            @Suppress("UNUSED_VARIABLE") var actualToolPath = pathsService.toolPath
            Assert.fail("Exception was not thrown.")
        }
        catch (ex: RunBuildException) {
        }
    }

    @Test
    fun shouldProvideToolPathWhenCustomToolPathIsNotAbsoluteExecutable() {
        // Given
        val pathsService = createInstance()
        val path = File("custom_bazel")
        val checkoutPath = File("checkoutDir")
        val absolutePath = File(checkoutPath, path.path)

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { _fileSystemService.isAbsolute(path) } returns false
        every { _buildStepContext.runnerContext } returns mockk<BuildRunnerContext> {
            every { build } returns mockk<AgentRunningBuild> {
                every { checkoutDirectory } returns checkoutPath
            }
        }
        every { _fileSystemService.isDirectory(absolutePath) } returns false
        every { _fileSystemService.isExists(absolutePath) } returns true

        val actualToolPath = pathsService.toolPath

        // Then
        Assert.assertEquals(actualToolPath, absolutePath)
    }

    @Test
    fun shouldProvideToolPathWhenCustomToolPathIsNotAbsoluteDirOnWindows() {
        // Given
        val pathsService = createInstance()
        val path = File("custom_bazel")
        val checkoutPath = File("checkoutDir")
        val absolutePath = File(checkoutPath, path.path)
        val absoluteExecutablePath = File(absolutePath, "bazel.exe")

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { _fileSystemService.isAbsolute(path) } returns false
        every { _buildStepContext.runnerContext } returns mockk<BuildRunnerContext> {
            every { build } returns mockk<AgentRunningBuild> {
                every { checkoutDirectory } returns checkoutPath
            }
        }
        every { _fileSystemService.isDirectory(absolutePath) } returns true
        every { _environment.osType } returns OSType.WINDOWS
        every { _fileSystemService.isExists(absoluteExecutablePath) } returns true

        val actualToolPath = pathsService.toolPath

        // Then
        Assert.assertEquals(actualToolPath, absoluteExecutablePath)
    }

    @Test
    fun shouldProvideToolPathWhenCustomToolPathIsAbsoluteDirOnWindows() {
        // Given
        val pathsService = createInstance()
        val path = File("custom_bazel")
        val executablePath = File(path, "bazel.exe")

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { _fileSystemService.isAbsolute(path) } returns true
        every { _fileSystemService.isDirectory(path) } returns true
        every { _environment.osType } returns OSType.WINDOWS
        every { _fileSystemService.isExists(executablePath) } returns true

        val actualToolPath = pathsService.toolPath

        // Then
        Assert.assertEquals(actualToolPath, executablePath)
    }

    @Test
    fun shouldProvideToolPathWhenCustomToolPathIsNotAbsoluteDirOnUnix() {
        // Given
        val pathsService = createInstance()
        val path = File("custom_bazel")
        val checkoutPath = File("checkoutDir")
        val absolutePath = File(checkoutPath, path.path)
        val absoluteExecutablePath = File(absolutePath, "bazel")

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { _fileSystemService.isAbsolute(path) } returns false
        every { _buildStepContext.runnerContext } returns mockk<BuildRunnerContext> {
            every { build } returns mockk<AgentRunningBuild> {
                every { checkoutDirectory } returns checkoutPath
            }
        }
        every { _fileSystemService.isDirectory(absolutePath) } returns true
        every { _environment.osType } returns OSType.UNIX
        every { _fileSystemService.isExists(absoluteExecutablePath) } returns true

        val actualToolPath = pathsService.toolPath

        // Then
        Assert.assertEquals(actualToolPath, absoluteExecutablePath)
    }

    @Test
    fun shouldProvideToolPathWhenCustomToolPathIsAbsoluteDirOnMac() {
        // Given
        val pathsService = createInstance()
        val path = File("custom_bazel")
        val executablePath = File(path, "bazel")

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { _fileSystemService.isAbsolute(path) } returns true
        every { _fileSystemService.isDirectory(path) } returns true
        every { _environment.osType } returns OSType.MAC
        every { _fileSystemService.isExists(executablePath) } returns true

        val actualToolPath = pathsService.toolPath

        // Then
        Assert.assertEquals(actualToolPath, executablePath)
    }

    @Test
    fun shouldProvideDifferentUniqueNames(){
        val pathsService = createInstance()

        val uniqueName1 = pathsService.uniqueName
        val uniqueName2 = pathsService.uniqueName

        // Then
        Assert.assertNotEquals(uniqueName1, uniqueName2)
    }

    private fun createInstance() = PathsServiceImpl(
            _buildStepContext,
            _buildAgentConfiguration,
            _buildAgentConfigurablePaths,
            _pluginDescriptor,
            _fileSystemService,
            _environment,
            _parametersService
    )
}