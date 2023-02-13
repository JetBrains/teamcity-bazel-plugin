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
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.TerminationAction
import java.io.File

class CommandExecutionAdapter(
        private val _bazelRunnerBuildService: BazelRunnerBuildService)
    : CommandExecution {
    private val _processListeners by lazy { _bazelRunnerBuildService.listeners }

    var result: BuildFinishedStatus? = null
        private set

    override fun isCommandLineLoggingEnabled() = _bazelRunnerBuildService.isCommandLineLoggingEnabled

    override fun makeProgramCommandLine(): ProgramCommandLine = _bazelRunnerBuildService.makeProgramCommandLine()

    override fun beforeProcessStarted() = _bazelRunnerBuildService.beforeProcessStarted()

    override fun processStarted(programCommandLine: String, workingDirectory: File) {
        _processListeners.forEach {
            it.processStarted(programCommandLine, workingDirectory)
        }
    }

    override fun onStandardOutput(text: String) = _processListeners.forEach { it.onStandardOutput(text) }

    override fun onErrorOutput(text: String) = _processListeners.forEach { it.onStandardOutput(text) }

    override fun interruptRequested(): TerminationAction = _bazelRunnerBuildService.interrupt()

    override fun processFinished(exitCode: Int) {
        _bazelRunnerBuildService.afterProcessFinished()

        _processListeners.forEach {
            it.processFinished(exitCode)
        }

        if (exitCode == 3) {
            _processListeners.forEach { it.onStandardOutput("Process finished with exit code $exitCode (some tests have failed). Reporting step success as all the tests have run.") }
            result = BuildFinishedStatus.FINISHED_SUCCESS
        }
        else {
            result = _bazelRunnerBuildService.getRunResult(exitCode)
        }

        if (result == BuildFinishedStatus.FINISHED_SUCCESS) {
            _bazelRunnerBuildService.afterProcessSuccessfullyFinished()
        }
    }

    companion object {
        private val LOG = Logger.getInstance(CommandExecutionAdapter::class.java.name)
    }
}