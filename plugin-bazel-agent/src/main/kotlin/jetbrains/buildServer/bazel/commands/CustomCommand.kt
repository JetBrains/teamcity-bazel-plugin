package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.bazel.ArgumentsProvider
import jetbrains.buildServer.bazel.BazelCommand
import jetbrains.buildServer.bazel.CommandArgument
import jetbrains.buildServer.bazel.CommandLineBuilder
import kotlin.coroutines.experimental.buildSequence

/**
 * Custom bazel command.
 */
class CustomCommand(override val command: String,
                    override val commandLineBuilder: CommandLineBuilder,
                    private val _commonArgumentsProvider: ArgumentsProvider) : BazelCommand {

    override val arguments: Sequence<CommandArgument>
        get() = buildSequence {
            yieldAll(_commonArgumentsProvider.getArguments(this@CustomCommand))
        }
}