package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.ProgramCommandLine

interface CommandLineExecutor {
    fun tryExecute(commandLine: ProgramCommandLine, executionTimeoutSeconds: Int = 60): Int
}