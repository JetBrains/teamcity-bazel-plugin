/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.AgentBuildRunnerInfo
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory
import org.springframework.beans.factory.BeanFactory

/**
 * Bazel runner service factory.
 */
class BazelBuildSessionFactory(
        private val _beanFactory: BeanFactory)
    : MultiCommandBuildSessionFactory, BuildStepContext {

    private var _runnerContext: BuildRunnerContext? = null

    override fun createSession(runnerContext: BuildRunnerContext): MultiCommandBuildSession {
        _runnerContext = runnerContext
        return _beanFactory.getBean(BazelCommandBuildSession::class.java)
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

    override val runnerContext: BuildRunnerContext
        get() = _runnerContext ?: throw RunBuildException("Runner session was not started")
}
