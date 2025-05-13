

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory
import jetbrains.buildServer.util.EventDispatcher
import org.springframework.beans.factory.BeanFactory
import java.io.Closeable

/**
 * Bazel runner service factory.
 */
class BazelBuildSessionFactory(
    listener: EventDispatcher<AgentLifeCycleListener>,
    private val _beanFactory: BeanFactory,
    private val _buildStepContext: BuildStepContext,
) : AgentLifeCycleAdapter(),
    MultiCommandBuildSessionFactory {
    private var sessionToken: Closeable? = null

    init {
        listener.addListener(this)
    }

    override fun createSession(runnerContext: BuildRunnerContext): MultiCommandBuildSession {
        sessionToken = _buildStepContext.startSession(runnerContext)
        return _beanFactory.getBean(BazelCommandBuildSession::class.java)
    }

    override fun buildFinished(
        build: AgentRunningBuild,
        buildStatus: BuildFinishedStatus,
    ) {
        try {
            super.buildFinished(build, buildStatus)
        } finally {
            sessionToken?.close()
        }
    }

    override fun getBuildRunnerInfo(): AgentBuildRunnerInfo =
        object : AgentBuildRunnerInfo {
            override fun getType(): String = BazelConstants.RUNNER_TYPE

            override fun canRun(config: BuildAgentConfiguration): Boolean = true
        }
}
