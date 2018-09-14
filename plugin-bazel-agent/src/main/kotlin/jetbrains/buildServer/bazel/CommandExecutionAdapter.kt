/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.bazel

import devteam.rx.Disposable
import devteam.rx.subscribe
import jetbrains.buildServer.SimpleCommandLineProcessRunner
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.*
import java.io.ByteArrayOutputStream
import java.io.File

class CommandExecutionAdapter(
        private val _processRunner: ProcessRunner,
        private val _bazelRunnerBuildService: BazelRunnerBuildService,
        private val _besRunnerBuildService: BesRunnerBuildService)
    : ByteArrayOutputStream(4096), CommandExecution, SimpleCommandLineProcessRunner.RunCommandEvents {
    private var _besToken: Disposable? = null
    private var _port: Int = 0
    private val _processListeners by lazy { _bazelRunnerBuildService.listeners }

    var result: BuildFinishedStatus? = null
        private set

    override fun isCommandLineLoggingEnabled() = _bazelRunnerBuildService.isCommandLineLoggingEnabled

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val commandLine = _bazelRunnerBuildService.makeProgramCommandLine()
        commandLine.arguments.add("--bes_backend=localhost:$_port")
        return commandLine
    }

    override fun beforeProcessStarted() {
        _port = 0
        val besCommandLine = _besRunnerBuildService.makeProgramCommandLine()
        val eventSource = _processRunner.run("Build Event Service", defaultOutputIdleSecondsTimeout, besCommandLine)
        val lockObject = java.lang.Object()
        _besToken = eventSource.subscribe(
                {
                    when (it) {
                        is StdOutProcessEvent -> {
                            val text = it.stdOutText
                            if (_port == 0) {
                                _port = portRegex.find(text)?.groups?.get(1)?.value?.toInt() ?: 0
                                if (_port != 0) {
                                    synchronized(lockObject) {
                                        lockObject.notifyAll()
                                    }
                                }
                            } else {
                                onStandardOutputInternal(text)
                            }
                        }

                        is StdErrProcessEvent -> onErrorOutputInternal(it.stdErrText)
                    }
                },
                {
                },
                {
                })

        synchronized(lockObject) {
            lockObject.wait()
        }

        _bazelRunnerBuildService.beforeProcessStarted()
    }

    override fun processStarted(programCommandLine: String, workingDirectory: File) {
        _processListeners.forEach {
            it.processStarted(programCommandLine, workingDirectory)
        }
    }

    override fun onStandardOutput(text: String) {
        onStandardOutputInternal("> $text$")
    }

    override fun onErrorOutput(text: String) {
        onErrorOutputInternal("> $text$")
    }

    override fun interruptRequested(): TerminationAction {
        return _bazelRunnerBuildService.interrupt()
    }

    override fun processFinished(exitCode: Int) {
        _besToken?.let {
            it.dispose()
            _besToken = null
        }

        _bazelRunnerBuildService.afterProcessFinished()

        _processListeners.forEach {
            it.processFinished(exitCode)
        }

        result = _besRunnerBuildService.getRunResult(exitCode)
        if (result == BuildFinishedStatus.FINISHED_SUCCESS) {
            _bazelRunnerBuildService.afterProcessSuccessfullyFinished()
        }
    }

    override fun onProcessStarted(ps: Process) {
    }

    override fun onProcessFinished(ps: Process) {
    }

    override fun getOutputIdleSecondsTimeout(): Int? {
        return defaultOutputIdleSecondsTimeout
    }

    private fun onStandardOutputInternal(text: String) {
        _processListeners.forEach {
            it.onStandardOutput(text)
        }
    }

    private fun onErrorOutputInternal(text: String) {
        _processListeners.forEach {
            it.onErrorOutput(text)
        }
    }

    companion object {
        private const val defaultOutputIdleSecondsTimeout = 60000
        private val portRegex = ".*BES:\\s(\\d+).*?".toRegex()
    }
}