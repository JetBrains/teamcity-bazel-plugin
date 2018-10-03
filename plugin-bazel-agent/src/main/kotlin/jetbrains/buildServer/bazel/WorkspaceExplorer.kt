package jetbrains.buildServer.bazel

import java.io.File

interface WorkspaceExplorer {
    fun tryFindWorkspace(path: File): Workspace?
}