package jetbrains.buildServer.bazel

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import java.util.*

/**
 * Cleans up local bazel disk caches.
 */
class BazelCacheCleaner(
        private val _workspaceRegistry: WorkspaceRegistry,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _multiCommandBuildSessionFactory: MultiCommandBuildSessionFactory)
    : DirectoryCleanersProvider {

    override fun getCleanerName(): String {
        return "Bazel Cache Cleaner"
    }

    override fun registerDirectoryCleaners(context: DirectoryCleanersProviderContext,
                                           registry: DirectoryCleanersRegistry) {
        _multiCommandBuildSessionFactory.createSession((context.runningBuild as AgentRunningBuildEx).currentRunnerContext)
        _workspaceRegistry.workspaces.forEach {
            LOG.info("Register a cleaner for the workspace $it")
            registry.addCleaner(it.path.absoluteFile, Date(it.path.lastModified()), WorkspaceCleaner(_workspaceRegistry, it, _commandLineExecutor))
        }
    }

    companion object {
        private val LOG = Logger.getInstance(BazelCacheCleaner::class.java.name)
    }

    private class WorkspaceCleaner(
            private val _workspaceRegistry: WorkspaceRegistry,
            private val _workspace: Workspace,
            private val _commandLineExecutor: CommandLineExecutor)
        : Runnable {
        override fun run() {
            LOG.info("Clean the workspace $_workspace")
            _workspaceRegistry.unregister(_workspace)
            _commandLineExecutor.tryExecute(_workspace.cleanCommandLine)
        }
    }
}