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
        private val _shutdownCommand: BazelCommand,
        private val _workspaceRegistry: WorkspaceRegistry)
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

        _workspaceExplorer.tryFindWorkspace(workingDirectory)?.let {
            val shutdownCommandLine = ShutdownCommandLine(commandLine, it)
            LOG.info("Bazel command \"${command.command}\" was registered, in workspace $it")
            _shutdownCommands.add(shutdownCommandLine)
            _workspaceRegistry.register(it)
        }
    }

    override fun dispose() = _subscriptionToken.dispose()

    private class ShutdownCommandLine(val commandLine: ProgramCommandLine, val workspace: Workspace) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ShutdownCommandLine

            if (workspace != other.workspace) return false

            return true
        }

        override fun hashCode(): Int {
            return workspace.hashCode()
        }
    }

    companion object {
        private val LOG = Logger.getInstance(ShutdownMonitor::class.java.name)
    }
}