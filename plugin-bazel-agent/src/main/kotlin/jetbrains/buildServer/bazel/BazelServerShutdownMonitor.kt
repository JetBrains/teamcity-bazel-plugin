package jetbrains.buildServer.bazel

import devteam.rx.Disposable
import devteam.rx.subscribe
import jetbrains.buildServer.agent.AgentLifeCycleEventSources
import jetbrains.buildServer.agent.CommandLineExecutor
import jetbrains.buildServer.agent.runner.*

class BazelServerShutdownMonitor(
        agentLifeCycleEventSources: AgentLifeCycleEventSources,
        commandLineExecutor: CommandLineExecutor,
        parametersService: ParametersService,
        pathsService: PathsService)
    : CommandRegistry {

    private var _subscriptionToken: Disposable
    private var _hasBazelCommand: Boolean = false

    init {
        _subscriptionToken = agentLifeCycleEventSources.buildFinishedSource.subscribe {
            if (_hasBazelCommand) {
                try {
                    // bazel shutdown
                    val envVars = parametersService.getParameterNames(ParameterType.Environment).associate { it to parametersService.tryGetParameter(ParameterType.Environment, it) }
                    val commandLine = SimpleProgramCommandLine(
                            envVars,
                            pathsService.getPath(PathType.WorkingDirectory).absolutePath,
                            pathsService.getToolPath(BazelConstants.BAZEL_CONFIG_NAME).absolutePath,
                            listOf(BazelConstants.COMMAND_SHUTDOWN))

                    commandLineExecutor.tryExecute(commandLine)
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