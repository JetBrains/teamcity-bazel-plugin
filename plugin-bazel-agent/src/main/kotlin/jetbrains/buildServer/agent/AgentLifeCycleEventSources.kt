

package jetbrains.buildServer.agent

import devteam.rx.Observable

interface AgentLifeCycleEventSources {
    val buildFinishedSource: Observable<BuildFinishedEvent>
    val beforeAgentConfigurationLoadedSource: Observable<BeforeAgentConfigurationLoadedEvent>

    data class BuildFinishedEvent(
        val build: AgentRunningBuild,
        val buildStatus: BuildFinishedStatus,
    )

    data class BeforeAgentConfigurationLoadedEvent(
        val agent: BuildAgent,
    )
}
