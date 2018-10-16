package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

class CommonArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsSplitter: BazelArgumentsSplitter,
        private val _startupArgumentsProvider: ArgumentsProvider) : ArgumentsProvider {
    override fun getArguments(command: BazelCommand): Sequence<CommandArgument> =
            buildSequence {
                _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_ARGUMENTS)?.trim()?.let {
                    yieldAll(_argumentsSplitter.splitArguments(it).map { argument ->
                        CommandArgument(CommandArgumentType.Argument, argument)
                    })
                }

                yieldAll(_startupArgumentsProvider.getArguments(command))
            }
}