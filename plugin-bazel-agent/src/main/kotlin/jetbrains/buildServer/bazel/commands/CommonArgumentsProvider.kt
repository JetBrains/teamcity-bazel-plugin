package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.bazel.*

class CommonArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsSplitter: BazelArgumentsSplitter,
        private val _startupArgumentsProvider: ArgumentsProvider) : ArgumentsProvider {
    override fun getArguments(command: BazelCommand): Sequence<CommandArgument> =
            sequence {
                _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_ARGUMENTS)?.trim()?.let {
                    yieldAll(_argumentsSplitter.splitArguments(it).map { argument ->
                        CommandArgument(CommandArgumentType.Argument, argument)
                    })
                }

                yieldAll(_startupArgumentsProvider.getArguments(command))
            }
}