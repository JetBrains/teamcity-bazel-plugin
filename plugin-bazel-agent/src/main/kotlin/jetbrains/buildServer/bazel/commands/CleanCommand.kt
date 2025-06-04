

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.*

/**
 * Provides arguments to bazel clean command.
 */
class CleanCommand(
    private val _commonArgumentsProvider: CommonArgumentsProvider,
) : BazelCommand {
    override val command: String = BazelConstants.COMMAND_CLEAN

    override val arguments: Sequence<CommandArgument>
        get() =
            sequence {
                yield(CommandArgument(CommandArgumentType.Command, command))
                yieldAll(_commonArgumentsProvider.getArguments(this@CleanCommand))
            }
}
