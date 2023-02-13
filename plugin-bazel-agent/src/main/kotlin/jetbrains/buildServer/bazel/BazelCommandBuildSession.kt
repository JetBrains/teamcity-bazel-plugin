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
