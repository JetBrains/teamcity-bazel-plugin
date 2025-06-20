

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
    fun shouldProvideToolPathWhenToolPathWasNotDefined() {
        // Given
        val pathsService = createInstance()

        // When
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns null
        every { buildStepContext.runnerContext } returns
            mockk<BuildRunnerContext> {
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
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { fileSystemService.isAbsolute(path) } returns true
        every { fileSystemService.isDirectory(path) } returns false
        every { fileSystemService.isExists(path) } returns true

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
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { fileSystemService.isAbsolute(path) } returns true
        every { fileSystemService.isDirectory(path) } returns false
        every { fileSystemService.isExists(path) } returns false

        // Then
        try {
            @Suppress("UNUSED_VARIABLE")
            var actualToolPath = pathsService.toolPath
            Assert.fail("Exception was not thrown.")
        } catch (ex: RunBuildException) {
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
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { fileSystemService.isAbsolute(path) } returns false
        every { buildStepContext.runnerContext } returns
            mockk<BuildRunnerContext> {
                every { build } returns
                    mockk<AgentRunningBuild> {
                        every { checkoutDirectory } returns checkoutPath
                    }
            }
        every { fileSystemService.isDirectory(absolutePath) } returns false
        every { fileSystemService.isExists(absolutePath) } returns true

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
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { fileSystemService.isAbsolute(path) } returns false
        every { buildStepContext.runnerContext } returns
            mockk<BuildRunnerContext> {
                every { build } returns
                    mockk<AgentRunningBuild> {
                        every { checkoutDirectory } returns checkoutPath
                    }
            }
        every { fileSystemService.isDirectory(absolutePath) } returns true
        every { environment.osType } returns OSType.WINDOWS
        every { fileSystemService.isExists(absoluteExecutablePath) } returns true

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
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { fileSystemService.isAbsolute(path) } returns true
        every { fileSystemService.isDirectory(path) } returns true
        every { environment.osType } returns OSType.WINDOWS
        every { fileSystemService.isExists(executablePath) } returns true

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
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { fileSystemService.isAbsolute(path) } returns false
        every { buildStepContext.runnerContext } returns
            mockk<BuildRunnerContext> {
                every { build } returns
                    mockk<AgentRunningBuild> {
                        every { checkoutDirectory } returns checkoutPath
                    }
            }
        every { fileSystemService.isDirectory(absolutePath) } returns true
        every { environment.osType } returns OSType.UNIX
        every { fileSystemService.isExists(absoluteExecutablePath) } returns true

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
        every { parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.TOOL_PATH) } returns path.path
        every { fileSystemService.isAbsolute(path) } returns true
        every { fileSystemService.isDirectory(path) } returns true
        every { environment.osType } returns OSType.MAC
        every { fileSystemService.isExists(executablePath) } returns true

        val actualToolPath = pathsService.toolPath

        // Then
        Assert.assertEquals(actualToolPath, executablePath)
    }

    @Test
    fun shouldProvideDifferentUniqueNames() {
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
