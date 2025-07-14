package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.*

class BazelCommandLineBuilder(
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService,
    private val _workingDirectoryProvider: WorkingDirectoryProvider,
    private val _argumentsConverter: ArgumentsConverter,
    private val _buildStepContext: BuildStepContext,
) {
    fun build(command: BazelCommand): ProgramCommandLine {
        val executable = getExecutablePath()
        val environmentVariables = getEnvironmentVariables()
        environmentVariables.getOrPut("HOME") { System.getProperty("user.home") }

        return SimpleProgramCommandLine(
            environmentVariables,
            _workingDirectoryProvider.workingDirectory.absolutePath,
            executable,
            _argumentsConverter.convert(command.arguments).toList(),
        )
    }

    private fun getEnvironmentVariables() =
        _parametersService
            .getParameterNames(ParameterType.Environment)
            .associateWith { _parametersService.tryGetParameter(ParameterType.Environment, it) }
            .toMutableMap()

    private fun getExecutablePath() =
        when {
            _buildStepContext.runnerContext.isVirtualContext -> BazelConstants.EXECUTABLE
            else -> _pathsService.toolPath
        }
}
