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

import jetbrains.buildServer.RunBuildException

class ArgumentsConverterImpl : ArgumentsConverter {
    override fun convert(arguments: Sequence<CommandArgument>): Sequence<String> =
            sequence {
                val commands = mutableListOf<String>()
                val args = mutableListOf<String>()
                val targets = mutableListOf<String>()
                for ((type, value) in arguments) {
                    when (type) {
                        CommandArgumentType.StartupOption -> yield(value)
                        CommandArgumentType.Command -> commands.add(value)
                        CommandArgumentType.Argument -> args.add(value)
                        CommandArgumentType.Target -> targets.add(value)
                    }
                }

                if (commands.isEmpty()) {
                    throw RunBuildException("The command was not specified.")
                }

                yieldAll(commands)
                yieldAll(args)
                if (targets.any()) {
                    yield(targetsSplitter)
                    yieldAll(targets)
                }
            }

    companion object {
        private const val targetsSplitter = "--"
    }
}