package jetbrains.buildServer.bazel

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.bazel.BazelConstants.PARAM_INTEGRATION_MODE
import jetbrains.buildServer.runner.JavaRunnerConstants
import jetbrains.buildServer.util.StringUtil
import java.io.File

class BesCommandLineBuilder(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _workingDirectoryProvider: WorkingDirectoryProvider,
        private val _argumentsConverter: ArgumentsConverter)
    : CommandLineBuilder {
    override fun build(command: BazelCommand): ProgramCommandLine {
        val sb = StringBuilder()
        sb.appendln(_pathsService.toolPath)
        for (arg in _argumentsConverter.convert(getArgs(command))) {
            sb.appendln(StringUtil.unquoteString(arg))
        }

        val bazelCommandFile = File(_pathsService.getPath(PathType.AgentTemp), _pathsService.uniqueName)
        bazelCommandFile.writeText(sb.toString())

        // get java executable
        val environmentVariables = _parametersService.getParameterNames(ParameterType.Environment).associate { it to _parametersService.tryGetParameter(ParameterType.Environment, it) }.toMutableMap()
        environmentVariables.getOrPut("HOME") { System.getProperty("user.home") }

        val systemProperties = _parametersService.getParameterNames(ParameterType.System).associate { it to _parametersService.tryGetParameter(ParameterType.System, it) }

        val explicitJavaHome: String = _parametersService.tryGetParameter(ParameterType.Runner, JavaRunnerConstants.TARGET_JDK_HOME) ?: ""
        val propsAndVars = environmentVariables + systemProperties
        val baseDir = _pathsService.getPath(PathType.Checkout).absolutePath
        val javaHome = JavaRunnerUtil.findJavaHome(explicitJavaHome, propsAndVars, baseDir) ?: throw RunBuildException("Unable to find Java Home")
        val javaExecutable = JavaRunnerUtil.findJavaExecutablePath(javaHome, propsAndVars, baseDir) ?: throw RunBuildException("Unable to find Java")

        // get tool jar
        val pluginDir = _pathsService.getPath(PathType.Plugin)
        val jarFile = File(File(pluginDir, "tools"), "plugin-bazel-event-service.jar")

        val besArgs = mutableListOf<String>("-jar", jarFile.absolutePath, "-c=${bazelCommandFile.absolutePath}")

        _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_VERBOSITY)?.trim()?.let {
            Verbosity.tryParse(it)?.let {
                besArgs.add("-l=${it.id}")
            }
        }

        _parametersService.tryGetParameter(ParameterType.Runner, PARAM_INTEGRATION_MODE)?.let {
            IntegrationMode.tryParse(it)?.let {
                when(it) {
                    IntegrationMode.BinaryFile -> {
                        besArgs.add("-f=${File(_pathsService.getPath(PathType.AgentTemp), _pathsService.uniqueName).absolutePath}")
                    }
                    else -> Unit
                }
            }
        }

        return SimpleProgramCommandLine(
                environmentVariables,
                _workingDirectoryProvider.workingDirectory.absolutePath,
                javaExecutable.absolutePath,
                besArgs)
    }

    private fun getArgs(command: BazelCommand): Sequence<CommandArgument> = sequence {
        yieldAll(command.arguments)
        _parametersService.tryGetParameter(ParameterType.System, "teamcity.buildType.id")?.let {
            if (!it.isBlank()) {
                yield(CommandArgument(CommandArgumentType.Argument, "--project_id=$it"))
            }
        }
    }
}