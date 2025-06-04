package jetbrains.buildServer.bazel

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class WorkingDirectoryProvider(
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService,
) {
    val workingDirectory: File
        get() =
            _parametersService.tryGetParameter(ParameterType.Runner, BazelConstants.PARAM_WORKING_DIR)?.let {
                try {
                    LOG.info("Getting the working directory \"$it\" from the runner parameter \"${BazelConstants.PARAM_WORKING_DIR}\"")
                    return@let File(it).absoluteFile
                } catch (ex: Throwable) {
                    throw RunBuildException("Invalid working directory", ex)
                }
            } ?: _pathsService.getPath(PathType.Checkout).absoluteFile

    companion object {
        private val LOG = Logger.getInstance(WorkingDirectoryProvider::class.java.name)
    }
}
