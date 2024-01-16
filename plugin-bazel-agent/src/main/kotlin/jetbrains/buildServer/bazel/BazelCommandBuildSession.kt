

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.CommandExecution
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession

/**
 * Bazel runner service.
 */
class BazelCommandBuildSession(
        private val _commandExecutionAdapter: CommandExecutionAdapter)
    : MultiCommandBuildSession {

    private var _commandsIterator: Iterator<CommandExecution> = emptySequence<CommandExecution>().iterator()

    override fun sessionStarted() {
        _commandsIterator = sequenceOf(_commandExecutionAdapter).iterator()
    }

    override fun getNextCommand(): CommandExecution? {
        if (_commandsIterator.hasNext()) {
            return _commandsIterator.next()
        }

        return null
    }

    override fun sessionFinished(): BuildFinishedStatus? {
        return _commandExecutionAdapter.result
    }
}