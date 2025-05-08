package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.bazel.BazelConstants.PARAM_INTEGRATION_MODE

class BazelCommandLineBuilder(
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService,
    private val _workingDirectoryProvider: WorkingDirectoryProvider,
    private val _argumentsConverter: ArgumentsConverter,
    private val _besCommandLineBuilder: BesCommandLineBuilder
) {
    fun build(command: BazelCommand): ProgramCommandLine {
        if (isBesIntegrationMode(command)) {
            return _besCommandLineBuilder.build(command)
        }

        val environmentVariables = _parametersService.getParameterNames(ParameterType.Environment)
            .associateWith { _parametersService.tryGetParameter(ParameterType.Environment, it) }
            .toMutableMap()
        environmentVariables.getOrPut("HOME") { System.getProperty("user.home") }

        return SimpleProgramCommandLine(
            environmentVariables,
            _workingDirectoryProvider.workingDirectory.absolutePath,
            _pathsService.toolPath.absolutePath,
            _argumentsConverter.convert(command.arguments).toList()
        )
    }

    private fun isBesIntegrationMode(command: BazelCommand) = BES_COMMANDS.contains(command.command)
            && integrationMode == IntegrationMode.BES

    private val integrationMode
        get() = _parametersService.tryGetParameter(ParameterType.Runner, PARAM_INTEGRATION_MODE)
            ?.let { IntegrationMode.tryParse(it) }
            ?: IntegrationMode.BES

    companion object {
        private val BES_COMMANDS = setOf(
            BazelConstants.COMMAND_BUILD,
            BazelConstants.COMMAND_TEST,
            BazelConstants.COMMAND_RUN
        )
    }
}