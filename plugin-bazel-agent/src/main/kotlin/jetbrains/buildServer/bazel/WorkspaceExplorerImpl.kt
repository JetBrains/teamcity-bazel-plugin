package jetbrains.buildServer.bazel

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.FileSystemService
import java.io.File

class WorkspaceExplorerImpl(
        private val _fileSystemService: FileSystemService)
    : WorkspaceExplorer {
    override fun tryFindWorkspace(path: File): Workspace? {
        var dir: File? = path

        while (dir != null) {
            if (!_fileSystemService.isDirectory(dir)) {
                LOG.info("\"$dir\" is not a directory")
                dir = dir.parentFile
                continue
            }

            LOG.info("Try finding the workspace \"${BazelConstants.WORKSPACE_FILE_NAME}\" in the directory \"$dir\"")

            val items = _fileSystemService.list(dir).toList()
            val workspaceFile = items.find { !_fileSystemService.isDirectory(it) && BazelConstants.WORKSPACE_FILE_NAME.equals(it.name, true) }
            if (workspaceFile != null) {
                LOG.info("The workspace \"$workspaceFile\" was found in the directory \"$dir\"")
                return Workspace(workspaceFile)
            }

            LOG.info("The workspace \"${BazelConstants.WORKSPACE_FILE_NAME}\" was not found in the directory \"$dir\"")
            dir = dir.parentFile
        }

        return null
    }

    companion object {
        private val LOG = Logger.getInstance(WorkspaceExplorerImpl::class.java.name)
    }
}