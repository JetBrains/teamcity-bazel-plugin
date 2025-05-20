package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.java.DockerJavaExecutableProvider
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession

/**
 * Bazel runner service.
 */
class BazelCommandBuildSession(
    bazelRunnerBuildService: BazelRunnerBuildService,
    private val _dockerJavaExecutableProvider: DockerJavaExecutableProvider,
    private val _buildStepContext: BuildStepContext,
) : MultiCommandBuildSession {
    private var commandsIterator: Iterator<CommandExecution> = emptySequence<CommandExecution>().iterator()
    private val commandExecutionAdapter = CommandExecutionAdapter(bazelRunnerBuildService)

    private val isVirtualContext get() = _buildStepContext.runnerContext.isVirtualContext

    override fun sessionStarted() {
        commandsIterator =
            sequence {
                if (isVirtualContext) {
                    yieldAll(_dockerJavaExecutableProvider.getCommandExecutionSequence())
                }
                yield(commandExecutionAdapter)
            }.iterator()
    }

    override fun getNextCommand(): CommandExecution? {
        if (commandsIterator.hasNext()) {
            return commandsIterator.next()
        }

        return null
    }

    override fun sessionFinished(): BuildFinishedStatus? = commandExecutionAdapter.result
}
