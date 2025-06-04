package jetbrains.buildServer.bazel

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.CommandLineExecutor
import jetbrains.buildServer.bazel.commands.FullCleanCommand
import jetbrains.buildServer.bazel.commands.InfoWorkspaceCommand
import java.io.File

class WorkspaceExplorer(
    private val _commandLineExecutor: CommandLineExecutor,
    private val _infoWorkspaceCommand: InfoWorkspaceCommand,
    private val _fullCleanCommand: FullCleanCommand,
    private val _commandLineBuilder: BazelCommandLineBuilder,
) {
    fun tryFindWorkspace(path: File): Workspace? {
        val commandLine = _commandLineBuilder.build(_infoWorkspaceCommand)
        val result = _commandLineExecutor.tryExecute(commandLine)
        if (result.exitCode == 0) {
            try {
                val workspace = Workspace(File(result.stdOut.trim()), _commandLineBuilder.build(_fullCleanCommand))
                LOG.info("The workspace \"${workspace.path}\" was found for \"$path\"")
                return workspace
            } catch (ex: Exception) {
                LOG.error("Cannot find a workspace for \"$path\".", ex)
            }
        }

        LOG.warn("The workspace \"${BazelConstants.WORKSPACE_FILE_NAME}\" was not found for \"$path\"")
        return null
    }

    companion object {
        private val LOG = Logger.getInstance(WorkspaceExplorer::class.java.name)
    }
}
