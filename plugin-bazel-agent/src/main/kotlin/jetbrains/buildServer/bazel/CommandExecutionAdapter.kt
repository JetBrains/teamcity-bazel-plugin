

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.TerminationAction
import java.io.File

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.runner.ProcessListenerAdapter
import jetbrains.buildServer.messages.DefaultMessagesInfo.createBlockEnd
import jetbrains.buildServer.messages.DefaultMessagesInfo.createBlockStart
import org.apache.log4j.Logger

class LoggingProcessListenerKotlin(
    private val buildLogger: BuildProgressLogger
) : ProcessListenerAdapter() {

    companion object {
        private val OUT_LOG: Logger = Logger.getLogger("teamcity.out")
    }

    override fun onStandardOutput(text: String) {
        logMessage(text)
    }

    override fun onErrorOutput(text: String) {
        logWarning(text)
    }

    override fun processStarted(programCommandLine: String, workingDirectory: File) {
        super.processStarted(programCommandLine, workingDirectory)

        buildLogger.getFlowLogger("STDOUT").logMessage(createBlockStart("STDOUT", "STDOUT"))
    }

    override fun processFinished(exitCode: Int) {
        super.processFinished(exitCode)

        buildLogger.getFlowLogger("STDOUT").logMessage(createBlockEnd("STDOUT", "STDOUT"))
    }

    private fun logMessage(message: String) {
       // buildLogger.message(taggedMessage)
        buildLogger.getFlowLogger("STDOUT").message(message)

    }

    private fun logWarning(message: String) {
        buildLogger.warning(message)
        OUT_LOG.warn(message)
    }
}


class CommandExecutionAdapter(
        private val _bazelRunnerBuildService: BazelRunnerBuildService)
    : CommandExecution {
    private val _processListeners = listOf(LoggingProcessListenerKotlin(_bazelRunnerBuildService.logger))

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

        result = _bazelRunnerBuildService.getRunResult(exitCode)

        if (result == BuildFinishedStatus.FINISHED_SUCCESS) {
            _bazelRunnerBuildService.afterProcessSuccessfullyFinished()
        }
    }
}