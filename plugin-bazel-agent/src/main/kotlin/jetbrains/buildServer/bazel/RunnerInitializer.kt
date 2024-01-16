

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