package jetbrains.buildServer.bazel

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandLineExecutor
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.bazel.commands.ShutdownCommand
import jetbrains.buildServer.util.EventDispatcher
import java.io.File

class ShutdownMonitor(
    events: EventDispatcher<AgentLifeCycleListener>,
    private val _commandLineExecutor: CommandLineExecutor,
    private val _workspaceExplorer: WorkspaceExplorer,
    private val _shutdownCommand: ShutdownCommand,
    private val _workspaceRegistry: WorkspaceRegistry,
    private val _commandLineBuilder: BazelCommandLineBuilder,
) : AgentLifeCycleAdapter() {
    private var shutdownCommands: MutableSet<ShutdownCommandLine> = mutableSetOf()

    init {
        events.addListener(this)
    }

    override fun beforeBuildFinish(
        build: AgentRunningBuild,
        buildStatus: BuildFinishedStatus,
    ) {
        if (shutdownCommands.any()) {
            try {
                for (shutdownCommand in shutdownCommands) {
                    _commandLineExecutor.tryExecute(shutdownCommand.commandLine)
                }
            } finally {
                shutdownCommands.clear()
            }
        }
    }

    fun register(command: BazelCommand) {
        val commandLine = _commandLineBuilder.build(_shutdownCommand)
        val workingDirectory = File(commandLine.workingDirectory)

        _workspaceExplorer.tryFindWorkspace(workingDirectory)?.let {
            val shutdownCommandLine = ShutdownCommandLine(commandLine, it)
            LOG.info("Bazel command \"${command.command}\" was registered, in workspace $it")
            shutdownCommands.add(shutdownCommandLine)
            _workspaceRegistry.register(it)
        }
    }

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
