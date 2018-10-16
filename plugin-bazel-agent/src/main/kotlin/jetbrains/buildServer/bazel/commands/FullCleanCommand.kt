package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.*
import kotlin.coroutines.experimental.buildSequence

class FullCleanCommand(
        override val commandLineBuilder: CommandLineBuilder,
        private val _startupArgumentsProvider: ArgumentsProvider)
    : BazelCommand {

    override val command: String = "CleanExpunge"

    override val arguments: Sequence<CommandArgument>
        get() = buildSequence {
            yield(CommandArgument(CommandArgumentType.Command, BazelConstants.COMMAND_CLEAN))
            yieldAll(_startupArgumentsProvider.getArguments(this@FullCleanCommand))

            // Removes the entire working tree for this bazel instance, which includes all bazel-created temporary and build output files, and stops the bazel server if it is running.
            yield(CommandArgument(CommandArgumentType.Argument, "--expunge"))
        }
}