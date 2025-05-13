

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.BuildRunnerContext
import java.io.Closeable

class BuildStepContextImpl : BuildStepContext {
    private var _runnerContext: BuildRunnerContext? = null

    override val runnerContext: BuildRunnerContext
        get() = _runnerContext ?: throw RunBuildException("Runner session was not started")

    override fun startSession(runnerContext: BuildRunnerContext): Closeable {
        _runnerContext = runnerContext
        return Closeable { _runnerContext = null }
    }
}
