package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.bazel.*
import kotlin.coroutines.experimental.buildSequence

class TargetsArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsSplitter: BazelArgumentsSplitter)
    : ArgumentsProvider {
    override fun getArguments(command: BazelCommand): Sequence<CommandArgument>  = buildSequence {
        _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_TARGETS)?.let {
            if (it.isNotBlank()) {
                yieldAll(_argumentsSplitter.splitArguments(it).map { target ->
                    CommandArgument(CommandArgumentType.Target, target)
                })
            }
        }
    }
}