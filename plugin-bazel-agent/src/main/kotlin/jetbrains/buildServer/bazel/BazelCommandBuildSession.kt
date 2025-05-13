

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession

/**
 * Bazel runner service.
 */
class BazelCommandBuildSession(
    private val _commandExecutionAdapter: CommandExecutionAdapter,
) : MultiCommandBuildSession {
    private var commandsIterator: Iterator<CommandExecution> = emptySequence<CommandExecution>().iterator()

    override fun sessionStarted() {
        commandsIterator = sequenceOf(_commandExecutionAdapter).iterator()
    }

    override fun getNextCommand(): CommandExecution? {
        if (commandsIterator.hasNext()) {
            return commandsIterator.next()
        }

        return null
    }

    override fun sessionFinished(): BuildFinishedStatus? = _commandExecutionAdapter.result
}
