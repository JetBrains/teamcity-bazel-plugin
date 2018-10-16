package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
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

    private fun createInstance(): ToolProvider =
            BazelToolProvider(
                    _toolProvidersRegistry,
                    AgentEventDispatcher(),
                    _environment,
                    _fileSystemService,
                    _commandLineExecutor)
}