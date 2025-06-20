package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.bazel.BazelConstants.COMMAND_BUILD
import jetbrains.buildServer.bazel.BazelConstants.COMMAND_RUN
import jetbrains.buildServer.bazel.BazelConstants.COMMAND_TEST

/**
 * Bazel runner service.
 */
class BazelRunnerBuildService(
    buildStepContext: BuildStepContext,
    private val _shutdownMonitor: ShutdownMonitor,
    private val _commandFactory: BazelCommandFactory,
    private val _bazelCommandLineBuilder: BazelCommandLineBuilder,
    private val _besCommandLineBuilder: BesCommandLineBuilder,
) : BuildServiceAdapter() {
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
            _shutdownMonitor.register(command)
        }

        if (BES_COMMANDS.contains(commandName)) {
            return _besCommandLineBuilder.build(command)
        }
        return _bazelCommandLineBuilder.build(command)
    }

    override fun getRunResult(exitCode: Int): BuildFinishedStatus {
        if (getCommandName() == COMMAND_TEST) {
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

    private fun getCommandName() = runnerParameters[BazelConstants.PARAM_COMMAND]?.trim()

    companion object {
        private val BES_COMMANDS =
            setOf(
                COMMAND_BUILD,
                COMMAND_TEST,
                COMMAND_RUN,
            )
    }
}
