

package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.bazel.BazelConstants.COMMAND_BUILD
import jetbrains.buildServer.bazel.BazelConstants.COMMAND_RUN
import jetbrains.buildServer.bazel.BazelConstants.COMMAND_TEST

class BazelCommandLineBuilder(
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService,
    private val _workingDirectoryProvider: WorkingDirectoryProvider,
    private val _argumentsConverter: ArgumentsConverter,
    private val _besCommandLineBuilder: BesCommandLineBuilder,
) {
    fun build(command: BazelCommand): ProgramCommandLine {
        if (BEP_COMMANDS.contains(command.command)) {
            return _besCommandLineBuilder.build(command)
        }

        val environmentVariables =
            _parametersService
                .getParameterNames(ParameterType.Environment)
                .associate {
                    it to
                        _parametersService.tryGetParameter(ParameterType.Environment, it)
                }.toMutableMap()
        environmentVariables.getOrPut("HOME") { System.getProperty("user.home") }

        return SimpleProgramCommandLine(
            environmentVariables,
            _workingDirectoryProvider.workingDirectory.absolutePath,
            _pathsService.toolPath.absolutePath,
            _argumentsConverter.convert(command.arguments).toList(),
        )
    }

    companion object {
        private val BEP_COMMANDS =
            setOf(
                COMMAND_BUILD,
                COMMAND_TEST,
                COMMAND_RUN,
            )
    }
}
