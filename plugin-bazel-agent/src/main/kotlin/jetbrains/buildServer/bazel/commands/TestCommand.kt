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

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.bazel.*

/**
 * Provides arguments to bazel test command.
 */
class TestCommand(
        private val _parametersService: ParametersService,
        private val _argumentsSplitter: BazelArgumentsSplitter,
        override val commandLineBuilder: CommandLineBuilder,
        private val _buildArgumentsProvider: ArgumentsProvider)
    : BazelCommand {

    override val command: String = BazelConstants.COMMAND_TEST

    override val arguments: Sequence<CommandArgument>
        get() = sequence {
            yield(CommandArgument(CommandArgumentType.Command, command))
            yieldAll(_buildArgumentsProvider.getArguments(this@TestCommand))
        }
}
