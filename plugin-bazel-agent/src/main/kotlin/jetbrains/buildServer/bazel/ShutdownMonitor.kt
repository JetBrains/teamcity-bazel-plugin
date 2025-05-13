

package jetbrains.buildServer.bazel

import com.intellij.openapi.diagnostic.Logger
import devteam.rx.Disposable
import devteam.rx.observer
import jetbrains.buildServer.agent.AgentLifeCycleEventSources
import jetbrains.buildServer.agent.CommandLineExecutor
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import java.io.File

class ShutdownMonitor(
    agentLifeCycleEventSources: AgentLifeCycleEventSources,
    commandLineExecutor: CommandLineExecutor,
    private val _workspaceExplorer: WorkspaceExplorer,
    private val _shutdownCommand: BazelCommand,
    private val _workspaceRegistry: WorkspaceRegistry,
    private val _commandLineBuilder: BazelCommandLineBuilder,
) : CommandRegistry,
    Disposable {
    private var subscriptionToken: Disposable
    private var shutdownCommands: MutableSet<ShutdownCommandLine> = mutableSetOf()

    init {
        subscriptionToken =
            agentLifeCycleEventSources.buildFinishedSource.subscribe(
                observer(
                    onNext = { _ ->
                        if (shutdownCommands.any()) {
                            try {
                                for (shutdownCommand in shutdownCommands) {
                                    commandLineExecutor.tryExecute(shutdownCommand.commandLine)
                                }
                            } finally {
                                shutdownCommands.clear()
                            }
                        }
                    },
                    onError = { },
                    onComplete = {},
                ),
            )
    }

    override fun register(command: BazelCommand) {
        val commandLine = _commandLineBuilder.build(_shutdownCommand)
        val workingDirectory = File(commandLine.workingDirectory)

        _workspaceExplorer.tryFindWorkspace(workingDirectory)?.let {
            val shutdownCommandLine = ShutdownCommandLine(commandLine, it)
            LOG.info("Bazel command \"${command.command}\" was registered, in workspace $it")
            shutdownCommands.add(shutdownCommandLine)
            _workspaceRegistry.register(it)
        }
    }

    override fun dispose() = subscriptionToken.dispose()

    private class ShutdownCommandLine(
        val commandLine: ProgramCommandLine,
        val workspace: Workspace,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ShutdownCommandLine

            return workspace == other.workspace
        }

        override fun hashCode(): Int = workspace.hashCode()
    }

    companion object {
        private val LOG = Logger.getInstance(ShutdownMonitor::class.java.name)
    }
}
