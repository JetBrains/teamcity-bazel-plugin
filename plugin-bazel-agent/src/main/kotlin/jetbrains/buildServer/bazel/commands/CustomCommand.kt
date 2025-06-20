

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.BazelCommand
import jetbrains.buildServer.bazel.CommandArgument
import jetbrains.buildServer.bazel.CommandArgumentType

/**
 * Custom bazel command.
 */
class CustomCommand(
    override val command: String,
    private val _commonArgumentsProvider: CommonArgumentsProvider,
) : BazelCommand {
    override val arguments: Sequence<CommandArgument>
        get() =
            sequence {
                yield(CommandArgument(CommandArgumentType.Command, command))
                yieldAll(_commonArgumentsProvider.getArguments(this@CustomCommand))
            }
}
