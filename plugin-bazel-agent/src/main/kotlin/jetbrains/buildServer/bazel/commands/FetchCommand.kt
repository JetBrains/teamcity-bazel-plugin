

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.*

/**
 * Provides arguments to bazel fetch command.
 */
class FetchCommand(
    private val _commonArgumentsProvider: CommonArgumentsProvider,
    private val _targetsArgumentsProvider: TargetsArgumentsProvider,
) : BazelCommand {
    override val command: String = BazelConstants.COMMAND_FETCH

    override val arguments: Sequence<CommandArgument>
        get() =
            sequence {
                yield(CommandArgument(CommandArgumentType.Command, command))
                yieldAll(_commonArgumentsProvider.getArguments(this@FetchCommand))
                yieldAll(_targetsArgumentsProvider.getArguments(this@FetchCommand))
            }
}
