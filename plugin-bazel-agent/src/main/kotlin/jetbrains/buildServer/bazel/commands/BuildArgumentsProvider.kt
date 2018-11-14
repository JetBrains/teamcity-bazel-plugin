package jetbrains.buildServer.bazel.commands

import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.bazel.BazelCommand
import jetbrains.buildServer.bazel.BazelConstants
import jetbrains.buildServer.bazel.CommandArgument
import jetbrains.buildServer.bazel.CommandArgumentType
import kotlin.coroutines.experimental.buildSequence

class BuildArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _commonArgumentsProvider: ArgumentsProvider,
        private val _targetsArgumentsProvider: ArgumentsProvider)
    : ArgumentsProvider {
    override fun getArguments(command: BazelCommand): Sequence<CommandArgument> = buildSequence {
        _parametersService.tryGetBuildFeatureParameter(BazelConstants.BUILD_FEATURE_TYPE, BazelConstants.PARAM_REMOTE_CACHE)?.let {
            yieldAll(_targetsArgumentsProvider.getArguments(command))
            if (it.isNotBlank()) {
                yield(CommandArgument(CommandArgumentType.Argument, "--remote_http_cache=${it.trim()}"))
            }
        }
    }
}