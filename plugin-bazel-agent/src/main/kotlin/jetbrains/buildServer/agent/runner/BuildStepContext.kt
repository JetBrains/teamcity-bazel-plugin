

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.BuildRunnerContext
import java.io.Closeable

class BuildStepContext {
    private var _runnerContext: BuildRunnerContext? = null

    val runnerContext: BuildRunnerContext
        get() = _runnerContext ?: throw RunBuildException("Runner session was not started")

    fun startSession(runnerContext: BuildRunnerContext): Closeable {
        _runnerContext = runnerContext
        return Closeable { _runnerContext = null }
    }
}
