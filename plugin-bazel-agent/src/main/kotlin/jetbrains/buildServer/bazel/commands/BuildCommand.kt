

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.*

/**
 * Provides arguments to bazel build command.
 */
class BuildCommand(
    private val _buildArgumentsProvider: ArgumentsProvider,
) : BazelCommand {
    override val command: String = BazelConstants.COMMAND_BUILD

    override val arguments: Sequence<CommandArgument>
        get() =
            sequence {
                yield(CommandArgument(CommandArgumentType.Command, command))
                yieldAll(_buildArgumentsProvider.getArguments(this@BuildCommand))
            }
}
