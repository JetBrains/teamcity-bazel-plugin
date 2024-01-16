

package jetbrains.buildServer.agent

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import java.io.File

class CommandLineExecutorImpl : CommandLineExecutor {
    override fun tryExecute(commandLine: ProgramCommandLine?, executionTimeoutSeconds: Int): CommandLineResult {
        val cmd = GeneralCommandLine()
        if(commandLine != null) {
            cmd.exePath = commandLine.executablePath
            cmd.setWorkingDirectory(File(commandLine.workingDirectory))
            cmd.addParameters(commandLine.arguments)

            val currentEnvironment = commandLine.environment
            currentEnvironment.getOrPut("HOME") { System.getProperty("user.home") }
            cmd.envParams = currentEnvironment
        }

        LOG.info("Execute command line: \"${cmd.commandLineString}\" in the working directory \"${cmd.workDirectory}\"")
        val executor = jetbrains.buildServer.CommandLineExecutor(cmd)
        return executor.runProcess(executionTimeoutSeconds)?.let {
            LOG.info("Command execution exit code: \"${it.exitCode}\"")
            LOG.debug("Command execution stdOut:\"${it.stdout}\"")
            LOG.debug("Command execution stdErr:\"${it.stderr}\"")
            CommandLineResult(it.exitCode, it.stdout, it.stderr)
        } ?: throw RunBuildException("Cannot run \"${cmd.commandLineString}\" in the working directory \"${cmd.workDirectory}\"")
    }

    companion object {
        private val LOG = Logger.getInstance(CommandLineExecutorImpl::class.java.name)
    }
}