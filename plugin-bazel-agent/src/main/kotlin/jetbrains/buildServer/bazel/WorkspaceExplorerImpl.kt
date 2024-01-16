

package jetbrains.buildServer.bazel

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.CommandLineExecutor
import java.io.File

class WorkspaceExplorerImpl(
        private val _commandLineExecutor: CommandLineExecutor,
        private val _infoWorkspaceCommand: BazelCommand,
        private val _fullCleanCommand: BazelCommand)
    : WorkspaceExplorer {
    override fun tryFindWorkspace(path: File): Workspace? {
        val commandLine = _infoWorkspaceCommand.commandLineBuilder.build(_infoWorkspaceCommand)
        val result = _commandLineExecutor.tryExecute(commandLine)
        if (result.exitCode == 0) {
            try {
                val workspace = Workspace(File(result.stdOut.trim()), _fullCleanCommand.commandLineBuilder.build(_fullCleanCommand))
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
        private val LOG = Logger.getInstance(WorkspaceExplorerImpl::class.java.name)
    }
}