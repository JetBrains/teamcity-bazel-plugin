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
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Serializer
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import java.io.File

class WorkspaceRegistryImpl(
        pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _serializer: Serializer)
    : WorkspaceRegistry {

    private val _dataDir = File(pathsService.getPath(PathType.System), "bazel")
    private val _dataFile = File(_dataDir, "workspaces.json")

    override val workspaces: Sequence<Workspace>
        get() {
            var workspaces: WorkspacesDto? = null
            if (ensureDataFileExists()) {
                try {
                    _fileSystemService.read(_dataFile) {
                        workspaces = _serializer.tryDeserialize(WorkspacesDto::class.java, it)
                    }
                } catch (ex: Exception) {
                    LOG.error("Cannot load workspaces", ex)
                    try {
                        _fileSystemService.remove(_dataFile)
                    } catch (ex2: Exception) {
                        LOG.error("Cannot remove workspaces", ex2)
                    }
                }
            }

            return workspaces?.let {
                convertFromDto(it)
            } ?: emptySequence<Workspace>()
        }

    override fun register(workspace: Workspace) =
            saveWorkspaces(convertToDto(workspaces + sequenceOf(workspace)))

    override fun unregister(workspace: Workspace) =
            saveWorkspaces(convertToDto(workspaces - sequenceOf(workspace)))

    private fun convertToDto(workspaces: Sequence<Workspace>): WorkspacesDto =
            WorkspacesDto(workspaces.distinct().map {
                WorkspaceDto(
                        it.path.absolutePath,
                        ProgramCommandLineDto(
                                it.cleanCommandLine.environment.map { EnvironmentVariableDto(it.key, it.value) },
                                it.cleanCommandLine.workingDirectory,
                                it.cleanCommandLine.executablePath,
                                it.cleanCommandLine.arguments))
            }.toList())

    private fun convertFromDto(workspaces: WorkspacesDto): Sequence<Workspace> =
            workspaces.workspaces.asSequence().map {
                Workspace(
                        File(it.path),
                        SimpleProgramCommandLine(
                                it.cleanCommandLine.env.associate { it.name to it.value },
                                it.cleanCommandLine.workingDir,
                                it.cleanCommandLine.executablePath,
                                it.cleanCommandLine.arguments))
            }

    private fun saveWorkspaces(workspaces: WorkspacesDto) {
        try {
            ensureDataFileExists()
            _fileSystemService.write(_dataFile) {
                _serializer.serialize(workspaces, it)
            }
        } catch (ex: Exception) {
            LOG.error("Cannot save workspaces", ex)
        }
    }

    private fun ensureDataFileExists(): Boolean =
            if (_fileSystemService.isExists(_dataDir) && _fileSystemService.isDirectory(_dataDir)) {
                _fileSystemService.isExists(_dataFile) && !_fileSystemService.isDirectory(_dataFile)
            } else {
                _fileSystemService.createDirectory(_dataDir)
                false
            }

    companion object {
        private val LOG = Logger.getInstance(WorkspaceRegistryImpl::class.java.name)
    }

    data class WorkspaceDto(
            val path: String,
            val cleanCommandLine: ProgramCommandLineDto)

    data class WorkspacesDto(
            val workspaces: List<WorkspaceDto>)

    data class ProgramCommandLineDto(
            val env: List<EnvironmentVariableDto>,
            val workingDir: String,
            val executablePath: String,
            val arguments: List<String>)

    data class EnvironmentVariableDto(
            val name: String,
            val value: String)
}