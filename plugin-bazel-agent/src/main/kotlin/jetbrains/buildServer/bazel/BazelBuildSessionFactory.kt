/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
