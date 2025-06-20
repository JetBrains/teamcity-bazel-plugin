package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.java.AgentHostJavaExecutableProvider
import jetbrains.buildServer.agent.java.DockerJavaExecutableProvider
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.bazel.BazelConstants.PARAM_INTEGRATION_MODE
import jetbrains.buildServer.bazel.BazelConstants.PARAM_VERBOSITY
import jetbrains.buildServer.util.StringUtil
import java.io.File
import kotlin.io.path.absolutePathString

class BesCommandLineBuilder(
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService,
    private val _workingDirectoryProvider: WorkingDirectoryProvider,
    private val _argumentsConverter: ArgumentsConverter,
    private val _buildStepContext: BuildStepContext,
    private val _agentJavaExecutableProvider: AgentHostJavaExecutableProvider,
    private val _dockerJavaExecutableProvider: DockerJavaExecutableProvider,
) {
    private val virtualContext get() = _buildStepContext.runnerContext.virtualContext

    fun build(command: BazelCommand): ProgramCommandLine {
        val bazelCommandFile = createBazelCommandFile(command)

        val besJarFile =
            _pathsService
                .getPath(PathType.Plugin)
                .toPath()
                .resolve("tools")
                .resolve("plugin-bazel-event-service.jar")
        val besArgs =
            buildList(capacity = 6) {
                add("-Djava.io.tmpdir=${_pathsService.getPath(PathType.AgentTemp).absolutePath}")
                add("-jar")
                add(besJarFile.absolutePathString())
                add("-c=${bazelCommandFile.absolutePath}")

                verbosity?.let {
                    add("-l=${it.id}")
                }

                if (integrationMode == IntegrationMode.BinaryFile) {
                    val binaryFile =
                        _pathsService.getPath(PathType.AgentTemp).toPath().resolve(_pathsService.uniqueName)
                    add("-f=${binaryFile.absolutePathString()}")
                }
            }

        val javaExecutable =
            when {
                virtualContext.isVirtual -> _dockerJavaExecutableProvider.getJavaExecutable()
                else -> _agentJavaExecutableProvider.getJavaExecutable()
            }
        return SimpleProgramCommandLine(
            environmentVariables,
            _workingDirectoryProvider.workingDirectory.absolutePath,
            javaExecutable,
            besArgs,
        )
    }

    private fun createBazelCommandFile(command: BazelCommand): File {
        val sb =
            buildString {
                appendLine(_pathsService.toolPath)
                for (arg in _argumentsConverter.convert(getArgs(command).asSequence())) {
                    val unquotedArg = StringUtil.unquoteString(arg)
                    val resolvedPath =
                        when {
                            virtualContext.isVirtual -> virtualContext.resolvePath(unquotedArg)
                            else -> unquotedArg
                        }
                    appendLine(resolvedPath)
                }
            }

        val bazelCommandFile = File(_pathsService.getPath(PathType.AgentTemp), _pathsService.uniqueName)
        bazelCommandFile.writeText(sb)
        return bazelCommandFile
    }

    private fun getArgs(command: BazelCommand) =
        command
            .arguments
            .toMutableList()
            .also { addBesInstanceNameArg(it) }

    private fun addBesInstanceNameArg(args: MutableList<CommandArgument>) {
        // --bes_instance_name and --project_id (deprecated) are the same thing needed for BES integration mode
        val hasProjectId =
            args.any {
                it.value.startsWith("--project_id=") ||
                    it.value.startsWith("--bes_instance_name")
            }
        if (integrationMode == IntegrationMode.BES && !hasProjectId) {
            buildTypeId?.let {
                // we add deprecated --project_id, but not --bes_instance_name for broader backward compatibility
                args.add(CommandArgument(CommandArgumentType.Argument, "--project_id=$it"))
            }
        }
    }

    private val buildTypeId
        get() =
            _parametersService
                .tryGetParameter(ParameterType.System, "teamcity.buildType.id")
                ?.takeIf { !it.isBlank() }

    private val environmentVariables
        get() =
            _parametersService
                .getParameterNames(ParameterType.Environment)
                .associateWith { _parametersService.tryGetParameter(ParameterType.Environment, it) }
                .toMutableMap()
                .also { it.getOrPut("HOME") { System.getProperty("user.home") } }

    private val verbosity
        get() =
            _parametersService
                .tryGetParameter(ParameterType.Runner, PARAM_VERBOSITY)
                ?.trim()
                ?.let { Verbosity.tryParse(it) }

    private val integrationMode
        get() =
            _parametersService
                .tryGetParameter(ParameterType.Runner, PARAM_INTEGRATION_MODE)
                ?.trim()
                ?.let { IntegrationMode.tryParse(it) }
                ?: IntegrationMode.default
}
