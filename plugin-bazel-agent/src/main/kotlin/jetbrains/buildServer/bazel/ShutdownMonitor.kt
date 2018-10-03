package jetbrains.buildServer.bazel

import com.intellij.openapi.diagnostic.Logger
import devteam.rx.Disposable
import devteam.rx.subscribe
import jetbrains.buildServer.agent.AgentLifeCycleEventSources
import jetbrains.buildServer.agent.CommandLineExecutor
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import java.io.File

class ShutdownMonitor(
        agentLifeCycleEventSources: AgentLifeCycleEventSources,
        commandLineExecutor: CommandLineExecutor,
        private val _workspaceExplorer: WorkspaceExplorer,
        private val _shutdownCommand: BazelCommand)
    : CommandRegistry, Disposable {

    private var _subscriptionToken: Disposable
    private var _shutdownCommands: MutableSet<ShutdownCommandLine> = mutableSetOf()

    init {
        _subscriptionToken = agentLifeCycleEventSources.buildFinishedSource.subscribe {
            if (_shutdownCommands.any()) {
                try {
                    for (shutdownCommand in _shutdownCommands) {
                        commandLineExecutor.tryExecute(shutdownCommand.commandLine)
                    }
                } finally {
                    _shutdownCommands.clear()
                }
            }
        }
    }

    override fun register(command: BazelCommand) {
        val commandLine = _shutdownCommand.commandLineBuilder.build(_shutdownCommand)
        val workingDirectory = File(commandLine.workingDirectory)
        val workspaceDirectory = _workspaceExplorer.tryFindWorkspace(workingDirectory)?.workspace?.parentFile ?: workingDirectory
        LOG.info("Bazel command \"${command.command}\" was registered, workspace directory is \"$workspaceDirectory\"")
        _shutdownCommands.add(ShutdownCommandLine(commandLine, workspaceDirectory))
    }

    override fun dispose() = _subscriptionToken.dispose()

    private class ShutdownCommandLine(val commandLine: ProgramCommandLine, private val _workspaceDirectory: File) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ShutdownCommandLine

            if (_workspaceDirectory != other._workspaceDirectory) return false

            return true
        }

        override fun hashCode(): Int {
            return _workspaceDirectory.hashCode()
        }
    }

    companion object {
        private val LOG = Logger.getInstance(ShutdownMonitor::class.java.name)
    }
}