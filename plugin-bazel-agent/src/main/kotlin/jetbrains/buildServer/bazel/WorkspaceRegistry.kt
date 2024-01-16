

package jetbrains.buildServer.bazel

interface WorkspaceRegistry {
    val workspaces: Sequence<Workspace>

    fun register(workspace: Workspace)

    fun unregister(workspace: Workspace)
}