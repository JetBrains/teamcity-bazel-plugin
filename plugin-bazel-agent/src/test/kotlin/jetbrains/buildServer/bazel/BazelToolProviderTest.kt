package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.ToolProvidersRegistry
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class BazelToolProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _toolProvidersRegistry: ToolProvidersRegistry

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _toolProvidersRegistry = _ctx.mock(ToolProvidersRegistry::class.java)
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
                    AgentEventDispatcher())
}