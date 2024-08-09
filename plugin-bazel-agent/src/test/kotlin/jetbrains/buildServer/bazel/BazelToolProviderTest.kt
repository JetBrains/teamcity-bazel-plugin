

package jetbrains.buildServer.bazel

import com.github.zafarkhaja.semver.Version
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class BazelToolProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _toolProvidersRegistry: ToolProvidersRegistry
    private lateinit var _environment: Environment
    private lateinit var _fileSystemService: FileSystemService
    private lateinit var _commandLineExecutor: CommandLineExecutor

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _toolProvidersRegistry = _ctx.mock(ToolProvidersRegistry::class.java)
        _environment = _ctx.mock(Environment::class.java)
        _fileSystemService = _ctx.mock(FileSystemService::class.java)
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
    }

    @Test
    fun shouldNotSearchToolInVirtualContext() {
        val build = _ctx.mock(AgentRunningBuild::class.java)
        val context = _ctx.mock(BuildRunnerContext::class.java)
        _ctx.checking(object : Expectations() {
            init {
                oneOf(_toolProvidersRegistry)!!.registerToolProvider(with(any(BazelToolProvider::class.java)))

                allowing(context).isVirtualContext
                will(returnValue(true))
            }
        })

        val toolProvider = createInstance()
        val path = toolProvider.getPath("bazel", build, context)

        Assert.assertEquals(path, "bazel")
    }

    @DataProvider
    fun testDataForVersions(): Array<Array<out Any?>> {
        return arrayOf(
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
                arrayOf("   ", null)
                )
    }

    @Test(dataProvider = "testDataForVersions")
    fun shouldParseVersion(line: String, expectedVersion: Version?) {
        // Given
        val context = _ctx.mock(BuildRunnerContext::class.java)
        _ctx.checking(object : Expectations() {
            init {
                oneOf(_toolProvidersRegistry)!!.registerToolProvider(with(any(BazelToolProvider::class.java)))

                allowing(context).isVirtualContext
                will(returnValue(true))
            }
        })

        val toolProvider = createInstance()

        // When
        val actualVersion = toolProvider.tryParseVersion(line)

        // Then
        Assert.assertEquals(actualVersion, expectedVersion)
    }

    @DataProvider
    fun testDataToFindVersions(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(CommandLineResult(
                        0,
                        "WARNING: --batch mode is deprecated. Please instead explicitly shut down your Bazel server using the command \"bazel shutdown\".\n" +
                                "INFO: Invocation ID: 3cc42df3-2475-42e5-8f1a-d69d81835b66\n" +
                                "Build label: 0.22.0- (@non-git)\n" +
                                "Build target: bazel-out/k8-opt/bin/src/main/java/com/google/devtools/build/lib/bazel/BazelServer_deploy.jar\n" +
                                "Build time: Tue Jan 29 18:22:45 2019 (1548786165)\n" +
                                "Build timestamp: 1548786165\n" +
                                "Build timestamp as int: 1548786165",
                        ""),
                        listOf(Version.parse("0.22.0"))),
                arrayOf(CommandLineResult(
                        0,
                        "WARNING: --batch mode is deprecated. Please instead explicitly shut down your Bazel server using the command \"bazel shutdown\".\n" +
                                "INFO: Invocation ID: 3cc42df3-2475-42e5-8f1a-d69d81835b66\n" +
                                "Build label: 0.22.0" +
                                "Build target: bazel-out/k8-opt/bin/src/main/java/com/google/devtools/build/lib/bazel/BazelServer_deploy.jar\n" +
                                "Build time: Tue Jan 29 18:22:45 2019 (1548786165)\n" +
                                "Build timestamp: 1548786165\n" +
                                "Build timestamp as int: 1548786165",
                        ""),
                        listOf(Version.parse("0.22.0"))),
                arrayOf(CommandLineResult(
                        0,
                        "abc",
                        ""),
                        emptyList<Version>()),
                arrayOf(CommandLineResult(
                        0,
                        "",
                        ""),
                        emptyList<Version>()),
                arrayOf(CommandLineResult(
                        1,
                        "WARNING: --batch mode is deprecated. Please instead explicitly shut down your Bazel server using the command \"bazel shutdown\".\n" +
                                "INFO: Invocation ID: 3cc42df3-2475-42e5-8f1a-d69d81835b66\n" +
                                "Build label: 0.22.0" +
                                "Build target: bazel-out/k8-opt/bin/src/main/java/com/google/devtools/build/lib/bazel/BazelServer_deploy.jar\n" +
                                "Build time: Tue Jan 29 18:22:45 2019 (1548786165)\n" +
                                "Build timestamp: 1548786165\n" +
                                "Build timestamp as int: 1548786165",
                        "Error"),
                        emptyList<Version>())
        )
    }

    @Test(dataProvider = "testDataToFindVersions")
    fun shouldFindVersion(result: CommandLineResult, expectedVersions: List<Version>) {
        // Given
        val context = _ctx.mock(BuildRunnerContext::class.java)
        _ctx.checking(object : Expectations() {
            init {
                oneOf(_environment).tryGetEnvironmentVariable("PATH")
                will(returnValue("p1${File.pathSeparatorChar}${File.pathSeparatorChar}p2${File.pathSeparatorChar}  ${File.pathSeparatorChar}P3"))

                oneOf(_fileSystemService).isDirectory(File("p1"))
                will(returnValue(true))

                oneOf(_fileSystemService).isDirectory(File("p2"))
                will(returnValue(true))

                oneOf(_fileSystemService).isDirectory(File("P3"))
                will(returnValue(false))

                oneOf(_fileSystemService).list(File("p1"))
                will(returnValue(sequenceOf(File("bazel", "bazel.exec"))))

                oneOf(_fileSystemService).list(File("p2"))
                will(returnValue(sequenceOf(File("bazelb", "bazel"))))

                oneOf(_fileSystemService).isDirectory(File("p1/bazel"))
                will(returnValue(true))

                oneOf(_fileSystemService).isDirectory(File("bazel/bazel.exec"))
                will(returnValue(false))

                oneOf(_fileSystemService).isDirectory(File("bazelb/bazelb"))
                will(returnValue(false))

                oneOf(_fileSystemService).isDirectory(File("bazelb/bazel"))
                will(returnValue(false))

                oneOf(_commandLineExecutor).tryExecute(with(any(SimpleProgramCommandLine::class.java)), with(any(Int::class.java)))
                will(returnValue(result))

                allowing(_environment).EnvironmentVariables
                will(returnValue(mapOf("var" to "val")))

                oneOf(_toolProvidersRegistry)!!.registerToolProvider(with(any(BazelToolProvider::class.java)))

                allowing(context).isVirtualContext
                will(returnValue(true))
            }
        })

        val toolProvider = createInstance()

        // When
        val actualVersion = toolProvider.findVersions().toList()

        // Then
        Assert.assertEquals(actualVersion, expectedVersions.map { Pair(File("bazelb/bazel"), it) }.toList())
    }

    private fun createInstance(): BazelToolProvider =
            BazelToolProvider(
                    _toolProvidersRegistry,
                    AgentEventDispatcher(),
                    _environment,
                    _fileSystemService,
                    _commandLineExecutor)


}