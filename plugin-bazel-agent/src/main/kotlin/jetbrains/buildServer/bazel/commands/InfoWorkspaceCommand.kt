

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.BazelCommand
import jetbrains.buildServer.bazel.CommandArgument
import jetbrains.buildServer.bazel.CommandArgumentType

class InfoWorkspaceCommand(
    private val _startupArgumentsProvider: ArgumentsProvider,
) : BazelCommand {
    override val command: String = "InfoWorkspace"

    override val arguments: Sequence<CommandArgument>
        get() =
            sequence {
                yield(CommandArgument(CommandArgumentType.Command, "info"))
                yield(CommandArgument(CommandArgumentType.Command, "workspace"))
                yieldAll(_startupArgumentsProvider.getArguments(this@InfoWorkspaceCommand))
            }
}
