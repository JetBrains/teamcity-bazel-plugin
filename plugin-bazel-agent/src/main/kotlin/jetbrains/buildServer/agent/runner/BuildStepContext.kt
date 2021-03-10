package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.BuildRunnerContext
import java.io.Closeable

interface BuildStepContext {
    val runnerContext: BuildRunnerContext

    fun startSession(runnerContext: BuildRunnerContext): Closeable
}