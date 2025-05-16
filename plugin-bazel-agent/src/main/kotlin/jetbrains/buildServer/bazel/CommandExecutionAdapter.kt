

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.CommandLineBuildService
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.TerminationAction
import java.io.File

class CommandExecutionAdapter(
    private val _commandLineBuildService: CommandLineBuildService,
) : CommandExecution {
    private val processListeners by lazy { _commandLineBuildService.listeners }

    var result: BuildFinishedStatus? = null
        private set

    override fun isCommandLineLoggingEnabled() = _commandLineBuildService.isCommandLineLoggingEnabled

    override fun makeProgramCommandLine(): ProgramCommandLine = _commandLineBuildService.makeProgramCommandLine()

    override fun beforeProcessStarted() = _commandLineBuildService.beforeProcessStarted()

    override fun processStarted(
        programCommandLine: String,
        workingDirectory: File,
    ) {
        processListeners.forEach {
            it.processStarted(programCommandLine, workingDirectory)
        }
    }

    override fun onStandardOutput(text: String) = processListeners.forEach { it.onStandardOutput(text) }

    override fun onErrorOutput(text: String) = processListeners.forEach { it.onStandardOutput(text) }

    override fun interruptRequested(): TerminationAction = _commandLineBuildService.interrupt()

    override fun processFinished(exitCode: Int) {
        _commandLineBuildService.afterProcessFinished()

        processListeners.forEach {
            it.processFinished(exitCode)
        }

        result = _commandLineBuildService.getRunResult(exitCode)

        if (result == BuildFinishedStatus.FINISHED_SUCCESS) {
            _commandLineBuildService.afterProcessSuccessfullyFinished()
        }
    }
}
