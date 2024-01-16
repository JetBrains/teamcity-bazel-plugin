

package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.BuildServiceAdapter
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ProgramCommandLine

/**
 * Bazel runner service.
 */
class BazelRunnerBuildService(
        buildStepContext: BuildStepContext,
        private val _commandRegistry: CommandRegistry,
        private val _commandFactory: BazelCommandFactory) : BuildServiceAdapter() {

    init {
        initialize(buildStepContext.runnerContext.build, buildStepContext.runnerContext)
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val parameters = runnerParameters

        val commandName = parameters[BazelConstants.PARAM_COMMAND]?.trim()
        if (commandName == null || commandName.isEmpty()) {
            val buildException = RunBuildException("Bazel command name is empty")
            buildException.isLogStacktrace = false
            throw buildException
        }

        val command = _commandFactory.createCommand(commandName)

        // Register for shutdown only on build agent
        if (!runnerContext.isVirtualContext) {
            _commandRegistry.register(command)
        }

        return command.commandLineBuilder.build(command)
    }

    override fun isCommandLineLoggingEnabled() = false
}