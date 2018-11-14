package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.bazel.*
import kotlin.coroutines.experimental.buildSequence

class StartupArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsSplitter: BazelArgumentsSplitter) : ArgumentsProvider {
    override fun getArguments(command: BazelCommand): Sequence<CommandArgument> =
            buildSequence {
                _parametersService.tryGetBuildFeatureParameter(BazelConstants.BUILD_FEATURE_TYPE, BazelConstants.PARAM_STARTUP_OPTIONS)?.trim()?.let {
                    yieldAll(_argumentsSplitter.splitArguments(it).map { CommandArgument(CommandArgumentType.StartupOption, it) })
                }

                _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_STARTUP_OPTIONS)?.trim()?.let {
                    yieldAll(_argumentsSplitter.splitArguments(it).map { CommandArgument(CommandArgumentType.StartupOption, it) })
                }
            }
}