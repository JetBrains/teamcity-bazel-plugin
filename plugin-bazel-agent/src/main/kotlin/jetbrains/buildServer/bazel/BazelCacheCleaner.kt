/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.bazel

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory
import java.util.*

/**
 * Cleans up local bazel disk caches.
 */
class BazelCacheCleaner(
        private val _workspaceRegistry: WorkspaceRegistry,
        private val _commandLineExecutor: CommandLineExecutor,
        private val _buildStepContext: BuildStepContext)
    : DirectoryCleanersProvider {

    override fun getCleanerName(): String {
        return "Bazel Cache Cleaner"
    }

    override fun registerDirectoryCleaners(
            context: DirectoryCleanersProviderContext,
            registry: DirectoryCleanersRegistry) {
        _workspaceRegistry.workspaces.forEach {
            LOG.info("Register a cleaner for the workspace $it")
            registry.addCleaner(it.path.absoluteFile, Date(it.path.lastModified()), WorkspaceCleaner(_buildStepContext, context, _workspaceRegistry, it, _commandLineExecutor))
        }
    }

    companion object {
        private val LOG = Logger.getInstance(BazelCacheCleaner::class.java.name)
    }

    private class WorkspaceCleaner(
            private val _buildStepContext: BuildStepContext,
            private val _context: DirectoryCleanersProviderContext,
            private val _workspaceRegistry: WorkspaceRegistry,
            private val _workspace: Workspace,
            private val _commandLineExecutor: CommandLineExecutor)
        : Runnable {
        override fun run() {
            LOG.info("Clean the workspace $_workspace")
            _buildStepContext.startSession((_context.runningBuild as AgentRunningBuildEx).currentRunnerContext).use {
                _workspaceRegistry.unregister(_workspace)
                _commandLineExecutor.tryExecute(_workspace.cleanCommandLine)
            }
        }
    }
}