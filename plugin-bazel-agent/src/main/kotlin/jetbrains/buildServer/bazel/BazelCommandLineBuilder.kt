package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.*
import kotlin.coroutines.experimental.buildSequence

class BazelCommandLineBuilder(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _workingDirectoryProvider: WorkingDirectoryProvider,
        private val _argumentsSplitter: BazelArgumentsSplitter)
    : CommandLineBuilder {
    override fun build(command: BazelCommand): ProgramCommandLine {
        // get java executable
        val environmentVariables = _parametersService.getParameterNames(ParameterType.Environment).associate { it to _parametersService.tryGetParameter(ParameterType.Environment, it) }.toMutableMap()
        environmentVariables.getOrPut("HOME") { System.getProperty("user.home") }

        return SimpleProgramCommandLine(
                environmentVariables,
                _workingDirectoryProvider.workingDirectory.absolutePath,
                _pathsService.getToolPath(BazelConstants.BAZEL_CONFIG_NAME).absolutePath,
                getArgs(command).toList())
    }

    private fun getArgs(command: BazelCommand): Sequence<String> = buildSequence {
        yield(command.command)
        yieldAll(command.arguments)
        _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_ARGUMENTS)?.trim()?.let {
            yieldAll(_argumentsSplitter.splitArguments(it))
        }
    }
}