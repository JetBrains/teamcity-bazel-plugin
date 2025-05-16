package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.BazelCommand
import jetbrains.buildServer.bazel.CommandArgument
import jetbrains.buildServer.bazel.CommandArgumentType

class InfoJavaHomeCommand(
    private val _startupArgumentsProvider: ArgumentsProvider,
) : BazelCommand {
    override val command: String = "InfoJavaHome"

    override val arguments: Sequence<CommandArgument>
        get() =
            sequence {
                yield(CommandArgument(CommandArgumentType.Command, "info"))
                yield(CommandArgument(CommandArgumentType.Command, "java-home"))
                yieldAll(_startupArgumentsProvider.getArguments(this@InfoJavaHomeCommand))
            }
}
