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

import jetbrains.buildServer.bazel.*

class FullCleanCommand(
        override val commandLineBuilder: CommandLineBuilder,
        private val _startupArgumentsProvider: ArgumentsProvider)
    : BazelCommand {

    override val command: String = "CleanExpunge"

    override val arguments: Sequence<CommandArgument>
        get() = sequence {
            yield(CommandArgument(CommandArgumentType.Command, BazelConstants.COMMAND_CLEAN))
            yieldAll(_startupArgumentsProvider.getArguments(this@FullCleanCommand))

            // Removes the entire working tree for this bazel instance, which includes all bazel-created temporary and build output files, and stops the bazel server if it is running.
            yield(CommandArgument(CommandArgumentType.Argument, "--expunge"))
        }
}