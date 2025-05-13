

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.*

/**
 * Provides arguments to bazel run command.
 */
class RunCommand(
    private val _commonArgumentsProvider: ArgumentsProvider,
    private val _targetsArgumentsProvider: ArgumentsProvider,
) : BazelCommand {
    override val command: String = BazelConstants.COMMAND_RUN

    override val arguments: Sequence<CommandArgument>
        get() =
            sequence {
                yield(CommandArgument(CommandArgumentType.Command, command))
                yieldAll(_commonArgumentsProvider.getArguments(this@RunCommand))
                yieldAll(_targetsArgumentsProvider.getArguments(this@RunCommand))
            }
}
