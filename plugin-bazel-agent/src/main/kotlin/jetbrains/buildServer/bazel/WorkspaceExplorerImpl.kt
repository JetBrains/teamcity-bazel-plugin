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