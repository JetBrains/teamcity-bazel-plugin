

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
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.bazel.BazelConstants
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class PathsServiceTest {
    @MockK private lateinit var buildStepContext: BuildStepContext

    @MockK private lateinit var buildAgentConfiguration: BuildAgentConfiguration

    @MockK private lateinit var buildAgentConfigurablePaths: BuildAgentConfigurablePaths

    @MockK private lateinit var pluginDescriptor: PluginDescriptor

    @MockK private lateinit var fileSystemService: FileSystemService

    @MockK private lateinit var environment: Environment

    @MockK private lateinit var parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `should provide tool path when tool path was not defined`() {
        // arrange
        val pathsService = createInstance()

        // act
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns null
        every { buildStepContext.runnerContext } returns
            mockk<BuildRunnerContext> {
                every { getToolPath(BazelConstants.BAZEL_CONFIG_NAME) } returns "default_bazel"
            }

        val actualToolPath = pathsService.toolPath

        // assert
        assertEquals(actualToolPath, "default_bazel")
    }

    @Test
    fun `should skip checks for bazel executable name`() {
        // arrange
        val pathsService = createInstance()
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns "bazel"

        // act
        val actualToolPath = pathsService.toolPath

        // assert
        assertEquals(actualToolPath, "bazel")
    }

    @Test
    fun `should provide tool path when custom tool path is absolute executable`() {
        // arrange
        val pathsService = createInstance()
        val path = "custom_bazel"

        // act
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path
        fileSystemService.run {
            every { isAbsolute(File(path)) } returns true
            every { isDirectory(File(path)) } returns false
            every { isExists(File(path)) } returns true
        }

        val actualToolPath = pathsService.toolPath

        // assert
        assertEquals(actualToolPath, path)
    }

    @Test
    fun `should throw exception when executable does not exist`() {
        // arrange
        val pathsService = createInstance()
        val path = "custom_bazel"

        // act
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path
        fileSystemService.run {
            every { isAbsolute(File(path)) } returns true
            every { isDirectory(File(path)) } returns false
            every { isExists(File(path)) } returns false
        }

        // assert
        try {
            pathsService.toolPath
            Assert.fail("Exception was not thrown.")
        } catch (_: RunBuildException) {
        }
    }

    @Test
    fun `should provide tool path when custom tool path is not absolute executable`() {
        // arrange
        val pathsService = createInstance()
        val relativePath = File("custom_bazel")
        val checkoutPath = File("checkoutDir")
        val absolutePath = File(checkoutPath, relativePath.path)

        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns relativePath.path

        every { buildStepContext.runnerContext } returns
            mockk<BuildRunnerContext> {
                every { build } returns
                    mockk<AgentRunningBuild> {
                        every { checkoutDirectory } returns checkoutPath
                    }
            }

        fileSystemService.run {
            every { isAbsolute(relativePath) } returns false
            every { isDirectory(absolutePath) } returns false
            every { isExists(absolutePath) } returns true
        }

        // act
        val actualToolPath = pathsService.toolPath

        // assert
        assertEquals(actualToolPath, absolutePath.path)
    }

    @Test
    fun `should provide tool path when custom tool path is not absolute dir on windows`() {
        // arrange
        val pathsService = createInstance()
        val path = File("custom_bazel")
        val checkoutPath = File("checkoutDir")
        val absolutePath = File(checkoutPath, path.path)
        val absoluteExecutablePath = File(absolutePath, "bazel.exe")

        // act
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { environment.osType } returns OSType.WINDOWS
        every { buildStepContext.runnerContext } returns
            mockk<BuildRunnerContext> {
                every { build } returns
                    mockk<AgentRunningBuild> {
                        every { checkoutDirectory } returns checkoutPath
                    }
            }
        fileSystemService.run {
            every { isDirectory(absolutePath) } returns true
            every { isAbsolute(path) } returns false
            every { isExists(absoluteExecutablePath) } returns true
        }

        val actualToolPath = pathsService.toolPath

        // assert
        assertEquals(actualToolPath, absoluteExecutablePath.path)
    }

    @Test
    fun `should provide tool path when custom tool path is absolute dir on windows`() {
        // arrange
        val pathsService = createInstance()
        val path = File("custom_bazel")
        val executablePath = File(path, "bazel.exe")

        // act
        every { environment.osType } returns OSType.WINDOWS
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        fileSystemService.run {
            every { isAbsolute(path) } returns true
            every { isDirectory(path) } returns true
            every { isExists(executablePath) } returns true
        }
        val actualToolPath = pathsService.toolPath

        // assert
        assertEquals(actualToolPath, executablePath.path)
    }

    @Test
    fun `should provide tool path when custom tool path is not absolute dir on unix`() {
        // arrange
        val pathsService = createInstance()
        val path = File("custom_bazel")
        val checkoutPath = File("checkoutDir")
        val absolutePath = File(checkoutPath, path.path)
        val absoluteExecutablePath = File(absolutePath, "bazel")

        // act
        every { environment.osType } returns OSType.UNIX
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { buildStepContext.runnerContext } returns
            mockk<BuildRunnerContext> {
                every { build } returns
                    mockk<AgentRunningBuild> {
                        every { checkoutDirectory } returns checkoutPath
                    }
            }
        fileSystemService.run {
            every { isAbsolute(path) } returns false
            every { isDirectory(absolutePath) } returns true
            every { isExists(absoluteExecutablePath) } returns true
        }

        val actualToolPath = pathsService.toolPath

        // assert
        assertEquals(actualToolPath, absoluteExecutablePath.path)
    }

    @Test
    fun `should provide tool path when custom tool path is absolute dir on mac`() {
        // arrange
        val pathsService = createInstance()
        val path = File("custom_bazel")
        val executablePath = File(path, "bazel")

        // act
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        fileSystemService.run {
            every { isAbsolute(path) } returns true
            every { isDirectory(path) } returns true
            every { isExists(executablePath) } returns true
        }
        every { environment.osType } returns OSType.MAC

        val actualToolPath = pathsService.toolPath

        // assert
        assertEquals(actualToolPath, executablePath.path)
    }

    @Test
    fun `should provide different unique names`() {
        val pathsService = createInstance()

        val uniqueName1 = pathsService.uniqueName
        val uniqueName2 = pathsService.uniqueName

        // Then
        Assert.assertNotEquals(uniqueName1, uniqueName2)
    }

    private fun createInstance() =
        PathsService(
            buildStepContext,
            buildAgentConfiguration,
            buildAgentConfigurablePaths,
            pluginDescriptor,
            fileSystemService,
            environment,
            parametersService,
        )
}
