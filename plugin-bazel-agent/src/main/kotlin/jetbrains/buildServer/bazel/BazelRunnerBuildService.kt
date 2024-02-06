

package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.*

/**
 * Bazel runner service.
 */
class BazelRunnerBuildService(
        buildStepContext: BuildStepContext,
        private val _commandRegistry: CommandRegistry,
        private val _parametersService: ParametersService,
        private val _commandFactory: BazelCommandFactory) : BuildServiceAdapter() {

    init {
        initialize(buildStepContext.runnerContext.build, buildStepContext.runnerContext)
    }

    override fun makeProgramCommandLine(): ProgramCommandLine {
        val commandName = getCommandName()
        if (commandName.isNullOrEmpty()) {
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

    override fun getRunResult(exitCode: Int): BuildFinishedStatus {
        if (getCommandName() == "test") {
            if (exitCode == 3) {
                return BuildFinishedStatus.FINISHED_SUCCESS
            } else if (exitCode == 4) {
                val successWhenNoTests =
                        runnerParameters[BazelConstants.PARAM_SUCCESS_WHEN_NO_TESTS]?.trim()?.toBoolean()
                if (successWhenNoTests == true) {
                    return BuildFinishedStatus.FINISHED_SUCCESS
                }
            }
        }
        return super.getRunResult(exitCode)
    }

    override fun isCommandLineLoggingEnabled() = false

    private fun getCommandName() = runnerParameters[BazelConstants.PARAM_COMMAND]?.trim()
}