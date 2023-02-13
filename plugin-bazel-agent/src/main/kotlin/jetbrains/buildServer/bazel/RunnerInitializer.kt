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

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.util.positioning.PositionConstraint

class RunnerInitializer(
        events: EventDispatcher<AgentLifeCycleListener>)
    : PositionAware, AgentLifeCycleAdapter() {

    init {
        events.addListener(this)
    }

    override fun beforeRunnerStart(runner: BuildRunnerContext) {
        if (runner.runnerParameters[BazelConstants.PARAM_VERBOSITY]?.trim()?.let { Verbosity.tryParse(it) } != Verbosity.Diagnostic) {
            // apply quiet mode for test xml reports watcher
            runner.addRunnerParameter("xmlReportParsing.quietMode", "true")
        }

        super.beforeRunnerStart(runner)
    }

    override fun getOrderId(): String = ""

    override fun getConstraint(): PositionConstraint = PositionConstraint.first()
}