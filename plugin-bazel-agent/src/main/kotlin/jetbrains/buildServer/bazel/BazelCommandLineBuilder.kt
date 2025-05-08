package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.bazel.BazelConstants.PARAM_INTEGRATION_MODE
import java.io.File

class BazelCommandLineBuilder(
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService,
    private val _workingDirectoryProvider: WorkingDirectoryProvider,
    private val _argumentsConverter: ArgumentsConverter,
    private val _besCommandLineBuilder: BesCommandLineBuilder
) {
    fun build(command: BazelCommand): ProgramCommandLine {
        val commandArgs = command.arguments
        val integrationMode = getIntegrationMode()
        if (BEP_COMMANDS.contains(command.command)) {
            if (integrationMode == IntegrationMode.BES) {
                return _besCommandLineBuilder.build(command)
            }
            else {
                val binaryFile = File(_pathsService.getPath(PathType.AgentTemp), _pathsService.uniqueName).absolutePath
                commandArgs.plus(CommandArgument(CommandArgumentType.Argument, "--build_event_binary_file==$binaryFile"))
            }
        }

        val environmentVariables = _parametersService.getParameterNames(ParameterType.Environment)
            .associateWith { _parametersService.tryGetParameter(ParameterType.Environment, it) }
            .toMutableMap()
        environmentVariables.getOrPut("HOME") { System.getProperty("user.home") }

        return SimpleProgramCommandLine(
            environmentVariables,
            _workingDirectoryProvider.workingDirectory.absolutePath,
            _pathsService.toolPath.absolutePath,
            _argumentsConverter.convert(commandArgs).toList()
        )
    }

    private fun getIntegrationMode() = _parametersService.tryGetParameter(ParameterType.Runner, PARAM_INTEGRATION_MODE)
        ?.let { IntegrationMode.tryParse(it) }
        ?: IntegrationMode.BES

    companion object {
        private val BEP_COMMANDS = setOf(
            BazelConstants.COMMAND_BUILD,
            BazelConstants.COMMAND_TEST,
            BazelConstants.COMMAND_RUN
        )
    }
}