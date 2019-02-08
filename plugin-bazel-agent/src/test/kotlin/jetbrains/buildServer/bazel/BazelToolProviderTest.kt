package jetbrains.buildServer.bazel

import com.github.zafarkhaja.semver.Version
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

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
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("Build label: 0.22.0", Version.valueOf("0.22.0")),
                arrayOf("Build label: 0.22.0- (@non-git)", Version.valueOf("0.22.0")),
                arrayOf("Build label: 0.22a.0", null),
                arrayOf("Build label: abc", null),
                arrayOf("Build lab: 0.22.0", null),
                arrayOf("", null),
                arrayOf("   ", null)
                )
    }

    @Test(dataProvider = "testData")
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

    private fun createInstance(): BazelToolProvider =
            BazelToolProvider(
                    _toolProvidersRegistry,
                    AgentEventDispatcher(),
                    _environment,
                    _fileSystemService,
                    _commandLineExecutor)
}