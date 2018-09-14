package jetbrains.buildServer.agent.runner

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import devteam.rx.Observable
import devteam.rx.buildObservable
import devteam.rx.disposableOf
import jetbrains.buildServer.CommandLineExecutor
import jetbrains.buildServer.StreamGobbler

class ProcessRunnerImpl : ProcessRunner {
    override fun run(title: String, idleTimeoutSeconds: Int, commandLine: ProgramCommandLine): Observable<ProcessEvent> = buildObservable {
        val besGeneralCommandLine = GeneralCommandLine()
        besGeneralCommandLine.exePath = commandLine.executablePath
        besGeneralCommandLine.addParameters(commandLine.arguments)
        besGeneralCommandLine.envParams = commandLine.environment
        besGeneralCommandLine.setWorkDirectory(commandLine.workingDirectory)
        val process = besGeneralCommandLine.createProcess()
        val stdErr = OutputStreamToObserverAdapter(4096, ProcessEventType.StdErr)
        val stdOut = OutputStreamToObserverAdapter(4096, ProcessEventType.StdOut)
        val errGobbler = StreamGobbler(process.errorStream, null, title, stdErr)
        val outGobbler = StreamGobbler(process.inputStream, null, title, stdOut)
        var exitCode = 0

        disposableOf(
                disposableOf {
                    try {
                        exitCode = CommandLineExecutor.waitForProcess(process, title, errGobbler, outGobbler, idleTimeoutSeconds)
                    } catch (error: Exception) {
                        this.onError(error)
                    }
                },
                stdErr.subscribe(this),
                stdOut.subscribe(this),
                disposableOf {
                    try {
                        this.onNext(ExitCodeProcessEvent(exitCode))
                        this.onCompleted()
                    } catch (error: Exception) {
                        this.onError(error)
                    }
                })
    }

    companion object {
        private val LOG = Logger.getInstance(ProcessRunnerImpl::class.java.name)
    }
}