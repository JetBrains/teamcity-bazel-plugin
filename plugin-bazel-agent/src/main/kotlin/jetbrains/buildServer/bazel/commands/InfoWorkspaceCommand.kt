package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.*
import kotlin.coroutines.experimental.buildSequence

class InfoWorkspaceCommand(
        override val commandLineBuilder: CommandLineBuilder,
        private val _startupArgumentsProvider: ArgumentsProvider)
    : BazelCommand {

    override val command: String = "InfoWorkspace"

    override val arguments: Sequence<CommandArgument>
        get() = buildSequence {
            yield(CommandArgument(CommandArgumentType.Command, "info"))
            yield(CommandArgument(CommandArgumentType.Command, "workspace"))
            yieldAll(_startupArgumentsProvider.getArguments(this@InfoWorkspaceCommand))
        }
}