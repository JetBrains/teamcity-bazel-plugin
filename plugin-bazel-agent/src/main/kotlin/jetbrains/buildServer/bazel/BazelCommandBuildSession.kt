package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.java.DockerWrapperJavaExecutableLocator
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession

/**
 * Bazel runner service.
 */
class BazelCommandBuildSession(
    bazelRunnerBuildService: BazelRunnerBuildService,
    private val _javaExecutableLocator: DockerWrapperJavaExecutableLocator,
) : MultiCommandBuildSession {
    private var commandsIterator: Iterator<CommandExecution> = emptySequence<CommandExecution>().iterator()
    private val commandExecutionAdapter = CommandExecutionAdapter(bazelRunnerBuildService)

    override fun sessionStarted() {
        commandsIterator =
            sequence {
                yieldAll(_javaExecutableLocator.getCommandExecutionSequence())
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
