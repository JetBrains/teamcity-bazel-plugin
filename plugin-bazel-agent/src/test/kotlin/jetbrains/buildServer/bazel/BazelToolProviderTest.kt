

package jetbrains.buildServer.bazel

import com.github.zafarkhaja.semver.Version
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class BazelToolProviderTest {
    @MockK
    private lateinit var toolProvidersRegistry: ToolProvidersRegistry

    @MockK
    private lateinit var environment: Environment

    @MockK
    private lateinit var fileSystemService: FileSystemService

    @MockK
    private lateinit var commandLineExecutor: CommandLineExecutor

    @MockK
    private lateinit var buildRunnerContext: BuildRunnerContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { toolProvidersRegistry.registerToolProvider(any()) } returns Unit
        every { buildRunnerContext.isVirtualContext } returns true
    }

    @Test
    fun shouldNotSearchToolInVirtualContext() {
        val toolProvider = createInstance()
        val path = toolProvider.getPath("bazel", mockk<AgentRunningBuild>(), buildRunnerContext)

        Assert.assertEquals(path, "bazel")
    }

    @DataProvider
    fun testDataForVersions(): Array<Array<out Any?>> =
        arrayOf(
            arrayOf("Build label: 0.22.0", Version.parse("0.22.0")),
            arrayOf("Build label: 0.22.0.33", Version.parse("0.22.0")),
            arrayOf("Build label: 0.22", null),
            arrayOf("Build label: 22", null),
            arrayOf("Build label: 0.22.0- (@non-git)", Version.parse("0.22.0")),
            arrayOf("Build label: 0.22- (@non-git)", null),
            arrayOf("Build label: 0.22a.0", null),
            arrayOf("Build label: abc", null),
            arrayOf("Build lab: 0.22.0", null),
            arrayOf("", null),
            arrayOf("   ", null),
        )

    @Test(dataProvider = "testDataForVersions")
    fun shouldParseVersion(
        line: String,
        expectedVersion: Version?,
    ) {
        // Given
        val toolProvider = createInstance()

        // When
        val actualVersion = toolProvider.tryParseVersion(line)

        // Then
        Assert.assertEquals(actualVersion, expectedVersion)
    }

    @DataProvider
    fun testDataToFindVersions(): Array<Array<out Any?>> =
        arrayOf(
            arrayOf(
                CommandLineResult(
                    0,
                    "WARNING: --batch mode is deprecated. " +
                        "Please instead explicitly shut down your Bazel server using the command \"bazel shutdown\".\n" +
                        "INFO: Invocation ID: 3cc42df3-2475-42e5-8f1a-d69d81835b66\n" +
                        "Build label: 0.22.0- (@non-git)\n" +
                        "Build target: " +
                        "bazel-out/k8-opt/bin/src/main/java/com/google/devtools/build/lib/bazel/BazelServer_deploy.jar\n" +
                        "Build time: Tue Jan 29 18:22:45 2019 (1548786165)\n" +
                        "Build timestamp: 1548786165\n" +
                        "Build timestamp as int: 1548786165",
                    "",
                ),
                listOf(Version.parse("0.22.0")),
            ),
            arrayOf(
                CommandLineResult(
                    0,
                    "WARNING: --batch mode is deprecated. Please instead explicitly shut down " +
                        "your Bazel server using the command \"bazel shutdown\".\n" +
                        "INFO: Invocation ID: 3cc42df3-2475-42e5-8f1a-d69d81835b66\n" +
                        "Build label: 0.22.0" +
                        "Build target: bazel-out/k8-opt/bin/src/main/java/com/google/devtools/build/lib/bazel/BazelServer_deploy.jar\n" +
                        "Build time: Tue Jan 29 18:22:45 2019 (1548786165)\n" +
                        "Build timestamp: 1548786165\n" +
                        "Build timestamp as int: 1548786165",
                    "",
                ),
                listOf(Version.parse("0.22.0")),
            ),
            arrayOf(
                CommandLineResult(
                    0,
                    "abc",
                    "",
                ),
                emptyList<Version>(),
            ),
            arrayOf(
                CommandLineResult(
                    0,
                    "",
                    "",
                ),
                emptyList<Version>(),
            ),
            arrayOf(
                CommandLineResult(
                    1,
                    "WARNING: --batch mode is deprecated. " +
                        "Please instead explicitly shut down your Bazel server using the command \"bazel shutdown\".\n" +
                        "INFO: Invocation ID: 3cc42df3-2475-42e5-8f1a-d69d81835b66\n" +
                        "Build label: 0.22.0" +
                        "Build target: " +
                        "bazel-out/k8-opt/bin/src/main/java/com/google/devtools/build/lib/bazel/BazelServer_deploy.jar\n" +
                        "Build time: Tue Jan 29 18:22:45 2019 (1548786165)\n" +
                        "Build timestamp: 1548786165\n" +
                        "Build timestamp as int: 1548786165",
                    "Error",
                ),
                emptyList<Version>(),
            ),
        )

    @Test(dataProvider = "testDataToFindVersions")
    fun shouldFindVersion(
        result: CommandLineResult,
        expectedVersions: List<Version>,
    ) {
        // Given
        every { environment.tryGetEnvironmentVariable("PATH") } returns
            "p1${File.pathSeparatorChar}${File.pathSeparatorChar}p2${File.pathSeparatorChar}  ${File.pathSeparatorChar}P3"
        every { environment.environmentVariables } returns mapOf("var" to "val")

        fileSystemService.let {
            every { it.isDirectory(File("p1")) } returns true
            every { it.isDirectory(File("p2")) } returns true
            every { it.isDirectory(File("P3")) } returns false

            every { it.list(File("p1")) } returns sequenceOf(File("bazel", "bazel.exec"))
            every { it.list(File("p2")) } returns sequenceOf(File("bazelb", "bazel"))

            every { it.isDirectory(File("p1/bazel")) } returns true
            every { it.isDirectory(File("bazel/bazel.exec")) } returns false
            every { it.isDirectory(File("bazelb/bazelb")) } returns false
            every { it.isDirectory(File("bazelb/bazel")) } returns false
        }

        every { commandLineExecutor.tryExecute(any(), any()) } returns result

        val toolProvider = createInstance()

        // When
        val actualVersion = toolProvider.findVersions().toList()

        // Then
        Assert.assertEquals(actualVersion, expectedVersions.map { Pair(File("bazelb/bazel"), it) }.toList())
    }

    private fun createInstance(): BazelToolProvider =
        BazelToolProvider(
            toolProvidersRegistry,
            AgentEventDispatcher(),
            environment,
            fileSystemService,
            commandLineExecutor,
        )
}
