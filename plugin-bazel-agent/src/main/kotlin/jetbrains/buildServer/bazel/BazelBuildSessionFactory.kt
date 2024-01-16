

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory
import jetbrains.buildServer.util.EventDispatcher
import org.jetbrains.annotations.NotNull
import org.springframework.beans.factory.BeanFactory
import java.io.Closeable

/**
 * Bazel runner service factory.
 */
class BazelBuildSessionFactory(
        private val _beanFactory: BeanFactory,
        private val _listener: EventDispatcher<AgentLifeCycleListener>,
        private val _buildStepContext: BuildStepContext)
    : MultiCommandBuildSessionFactory, AgentLifeCycleAdapter() {

    private var _sessionToken: Closeable? = null

    init {
        _listener.addListener(this)
    }

    override fun createSession(runnerContext: BuildRunnerContext): MultiCommandBuildSession {
        _sessionToken = _buildStepContext.startSession(runnerContext)
        return _beanFactory.getBean(BazelCommandBuildSession::class.java)
    }

    override fun buildFinished(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
        try {
            super.buildFinished(build, buildStatus)
        }
        finally {
            _sessionToken?.close()
        }
    }

    override fun getBuildRunnerInfo(): AgentBuildRunnerInfo {
        return object : AgentBuildRunnerInfo {
            override fun getType(): String {
                return BazelConstants.RUNNER_TYPE
            }

            override fun canRun(config: BuildAgentConfiguration): Boolean {
                return true
            }
        }
    }
}