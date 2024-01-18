package jetbrains.buildServer.bazel

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Files

class BesCommandLineBuilderTest {
    @MockK private lateinit var _pathsService: PathsService
    private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _workingDirectoryProvider: WorkingDirectoryProvider
    @MockK private lateinit var _argumentsConverter: ArgumentsConverter
    @MockK private lateinit var _bazelCommand: BazelCommand
    private lateinit var tempDir: File

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        tempDir = Files.createTempDirectory(this.javaClass.name).toFile()
        tempDir.deleteOnExit()
    }

    @Test
    fun shouldGenerateEventServiceCall() {
        every { _pathsService.toolPath } returns File("/fake/bazel")
        every { _workingDirectoryProvider.workingDirectory } returns File("/fake/workdir")
        every { _pathsService.getPath(PathType.AgentTemp) } returns tempDir
        every { _pathsService.getPath(PathType.Checkout) } returns File("/fake/checkoutdir")
        every { _pathsService.getPath(PathType.Plugin) } returns File("/fake/plugindir")
        every { _pathsService.uniqueName } returns "unique-name-1" andThen "unique-name-2"
        every { _bazelCommand.arguments } returns
                sequenceOf(CommandArgument(CommandArgumentType.StartupOption, "foo"))
        every { _argumentsConverter.convert(any()) } returns sequenceOf("bar", "baz")
        _parametersService = ParametersServiceStub()
                .add(ParameterType.Runner, "integration", "BinaryFile")
        val fixture = BesCommandLineBuilder(
                _pathsService, _parametersService, _workingDirectoryProvider, _argumentsConverter
        )
        val besCommandLine = fixture.build(_bazelCommand)


        Assert.assertEquals(besCommandLine.arguments, listOf(
                "-jar",
            File("/fake/plugindir/tools/plugin-bazel-event-service.jar").absolutePath,
                "-c=${File(tempDir, "unique-name-1").absolutePath}",
                "-f=${File(tempDir, "unique-name-2").absolutePath}"
        ))
        val bazelCommandLine = File(tempDir, "unique-name-1")
        Assert.assertEquals(bazelCommandLine.readLines(), listOf(File("/fake/bazel").absolutePath, "bar", "baz"))
    }
}