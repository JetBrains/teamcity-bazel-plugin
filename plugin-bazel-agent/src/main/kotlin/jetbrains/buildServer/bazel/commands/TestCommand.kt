

package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.bazel.*

/**
 * Provides arguments to bazel test command.
 */
class TestCommand(
        private val _parametersService: ParametersService,
        private val _argumentsSplitter: BazelArgumentsSplitter,
        override val commandLineBuilder: CommandLineBuilder,
        private val _buildArgumentsProvider: ArgumentsProvider)
    : BazelCommand {

    override val command: String = BazelConstants.COMMAND_TEST

    override val arguments: Sequence<CommandArgument>
        get() = sequence {
            yield(CommandArgument(CommandArgumentType.Command, command))
            yieldAll(_buildArgumentsProvider.getArguments(this@TestCommand))
        }
}