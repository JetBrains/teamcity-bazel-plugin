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
        val stdErr = OutputStreamToObserverAdapter(8192, ProcessEventType.StdErr)
        val stdOut = OutputStreamToObserverAdapter(8192, ProcessEventType.StdOut)
        val errGobbler = StreamGobbler(process.errorStream, null, title, stdErr)
        val outGobbler = StreamGobbler(process.inputStream, null, title, stdOut)
        errGobbler.start()
        outGobbler.start()
        val subscription = disposableOf(stdErr.subscribe(this), stdOut.subscribe(this))

        disposableOf(
                disposableOf {
                    try {
                        val exitCode = CommandLineExecutor.waitForProcess(process, title, errGobbler, outGobbler, idleTimeoutSeconds)
                        this.onNext(ExitCodeProcessEvent(exitCode))
                        this.onCompleted()
                    } catch (error: Exception) {
                        this.onError(error)
                    }
                },
                subscription)
    }

    companion object {
        private val LOG = Logger.getInstance(ProcessRunnerImpl::class.java.name)
    }
}