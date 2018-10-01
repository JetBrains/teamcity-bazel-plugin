package jetbrains.buildServer.agent

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import java.io.File

class CommandLineExecutorImpl : CommandLineExecutor {
    override fun tryExecute(commandLine: ProgramCommandLine, executionTimeoutSeconds: Int): Int {
        val cmd = GeneralCommandLine()
        cmd.exePath = commandLine.executablePath
        cmd.setWorkingDirectory(File(commandLine.workingDirectory))
        cmd.addParameters(commandLine.arguments)

        val currentEnvironment = System.getenv().toMutableMap()
        for ((name, value) in commandLine.environment) {
            currentEnvironment[name] = value
        }

        currentEnvironment.getOrPut("HOME") { System.getProperty("user.home") }
        cmd.envParams = currentEnvironment

        LOG.info("Execute command line: ${cmd.commandLineString}")
        val executor = jetbrains.buildServer.CommandLineExecutor(cmd)
        return executor.runProcess(executionTimeoutSeconds)?.exitCode ?: -1
    }

    companion object {
        private val LOG = Logger.getInstance(CommandLineExecutorImpl::class.java.name)
    }
}