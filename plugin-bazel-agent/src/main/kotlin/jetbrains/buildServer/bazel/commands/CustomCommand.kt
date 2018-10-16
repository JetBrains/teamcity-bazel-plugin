package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.*
import kotlin.coroutines.experimental.buildSequence

/**
 * Custom bazel command.
 */
class CustomCommand(override val command: String,
                    override val commandLineBuilder: CommandLineBuilder,
                    private val _commonArgumentsProvider: ArgumentsProvider) : BazelCommand {

    override val arguments: Sequence<CommandArgument>
        get() = buildSequence {
            yield(CommandArgument(CommandArgumentType.Command, command))
            yieldAll(_commonArgumentsProvider.getArguments(this@CustomCommand))
        }
}