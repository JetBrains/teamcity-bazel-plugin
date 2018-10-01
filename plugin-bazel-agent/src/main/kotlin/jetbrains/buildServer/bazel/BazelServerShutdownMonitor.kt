package jetbrains.buildServer.bazel

import devteam.rx.Disposable
import devteam.rx.subscribe
import jetbrains.buildServer.agent.AgentLifeCycleEventSources
import jetbrains.buildServer.agent.CommandLineExecutor

class BazelServerShutdownMonitor(
        agentLifeCycleEventSources: AgentLifeCycleEventSources,
        commandLineExecutor: CommandLineExecutor,
        shutdownCommand: BazelCommand)
    : CommandRegistry {

    private var _subscriptionToken: Disposable
    private var _hasBazelCommand: Boolean = false

    init {
        _subscriptionToken = agentLifeCycleEventSources.buildFinishedSource.subscribe {
            if (_hasBazelCommand) {
                try {
                    commandLineExecutor.tryExecute(shutdownCommand.commandLineBuilder.build(shutdownCommand))
                } finally {
                    _hasBazelCommand = false
                }
            }
        }
    }

    override fun register(command: BazelCommand) {
        _hasBazelCommand = true
    }
}