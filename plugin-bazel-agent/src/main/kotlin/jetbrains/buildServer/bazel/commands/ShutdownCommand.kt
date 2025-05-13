

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.*

/**
 * Provides arguments to bazel shutdowm command.
 */
class ShutdownCommand(
    private val _startupArgumentsProvider: ArgumentsProvider,
) : BazelCommand {
    override val command: String = BazelConstants.COMMAND_SHUTDOWN

    override val arguments: Sequence<CommandArgument>
        get() =
            sequence {
                yield(CommandArgument(CommandArgumentType.Command, command))
                yieldAll(_startupArgumentsProvider.getArguments(this@ShutdownCommand))
            }
}
