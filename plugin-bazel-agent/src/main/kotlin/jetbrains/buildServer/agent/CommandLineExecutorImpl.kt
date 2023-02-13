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