package jetbrains.buildServer.agent.java

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.JavaRunnerUtil
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.runner.JavaRunnerConstants
import java.io.File

class JavaExecutableProvider(
    private val _buildStepContext: BuildStepContext,
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService,
) : AgentLifeCycleAdapter() {
    var containerJavaExecutable: String? = null

    fun getJavaExecutable(): String =
        when {
            _buildStepContext.runnerContext.isVirtualContext -> containerJavaExecutable ?: "java"
            else -> findJavaExecutable().absolutePath
        }

    private fun findJavaExecutable(): File {
        val systemProperties =
            _parametersService
                .getParameterNames(ParameterType.System)
                .associateWith { _parametersService.tryGetParameter(ParameterType.System, it) }
        val explicitJavaHome: String =
            _parametersService.tryGetParameter(ParameterType.Runner, JavaRunnerConstants.TARGET_JDK_HOME) ?: ""
        val environmentVariables = getEnvironmentVariables()
        val propsAndVars = environmentVariables + systemProperties
        val baseDir = _pathsService.getPath(PathType.Checkout).absolutePath
        val javaHome =
            JavaRunnerUtil.findJavaHome(explicitJavaHome, propsAndVars, baseDir)
                ?: throw RunBuildException("Unable to find Java Home")
        val javaExecutable =
            JavaRunnerUtil.findJavaExecutablePath(javaHome, propsAndVars, baseDir)
                ?: throw RunBuildException("Unable to find Java")
        return javaExecutable
    }

    private fun getEnvironmentVariables() =
        _parametersService
            .getParameterNames(ParameterType.Environment)
            .associateWith { _parametersService.tryGetParameter(ParameterType.Environment, it) }
            .toMap()

    override fun runnerFinished(
        runner: BuildRunnerContext,
        status: BuildFinishedStatus,
    ) {
        containerJavaExecutable = null
    }
}
