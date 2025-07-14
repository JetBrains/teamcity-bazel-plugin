package jetbrains.buildServer.bazel

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.java.AgentHostJavaExecutableProvider
import jetbrains.buildServer.agent.runner.BuildStepContext
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
    @MockK
    private lateinit var pathsService: PathsService
    private lateinit var parametersService: ParametersService

    @MockK
    private lateinit var buildStepContext: BuildStepContext

    @MockK
    private lateinit var javaExecutableProvider: AgentHostJavaExecutableProvider

    @MockK
    private lateinit var workingDirectoryProvider: WorkingDirectoryProvider

    @MockK
    private lateinit var argumentsConverter: ArgumentsConverter

    @MockK
    private lateinit var bazelCommand: BazelCommand
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
        every { pathsService.toolPath } returns "/fake/bazel"
        every { workingDirectoryProvider.workingDirectory } returns File("/fake/workdir")
        every { pathsService.getPath(PathType.AgentTemp) } returns tempDir
        every { pathsService.getPath(PathType.Checkout) } returns File("/fake/checkoutdir")
        every { pathsService.getPath(PathType.Plugin) } returns File("/fake/plugindir")
        every { pathsService.uniqueName } returns "bazelCommandlineFile" andThen "eventFile"
        every { bazelCommand.arguments } returns
            sequenceOf(CommandArgument(CommandArgumentType.StartupOption, "foo"))
        every { argumentsConverter.convert(any()) } returns sequenceOf("bar", "baz")
        parametersService =
            ParametersServiceStub()
                .add(ParameterType.Runner, "integration", "BinaryFile")
        every { buildStepContext.runnerContext } returns
            mockk {
                every { isVirtualContext } returns false
                every { virtualContext } returns
                    mockk {
                        every { isVirtual } returns false
                    }
            }
        every { javaExecutableProvider.getJavaExecutable() } returns "java"
        val fixture =
            BesCommandLineBuilder(
                pathsService,
                parametersService,
                workingDirectoryProvider,
                argumentsConverter,
                buildStepContext,
                javaExecutableProvider,
                mockk(),
            )
        val besCommandLine = fixture.build(bazelCommand)

        Assert.assertEquals(
            besCommandLine.arguments,
            listOf(
                "-Djava.io.tmpdir=${tempDir.absolutePath}",
                "-jar",
                File("/fake/plugindir/tools/plugin-bazel-event-service.jar").absolutePath,
                "-c=${File(tempDir, "bazelCommandlineFile").absolutePath}",
                "-f=${File(tempDir, "eventFile").absolutePath}",
            ),
        )
        val bazelCommandLine = File(tempDir, "bazelCommandlineFile")
        val bazelCommands = bazelCommandLine.readLines()
        Assert.assertEquals(bazelCommands, listOf(File("/fake/bazel").toString(), "bar", "baz"))
    }
}
