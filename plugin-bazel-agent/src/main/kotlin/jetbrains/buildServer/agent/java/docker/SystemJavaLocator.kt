package jetbrains.buildServer.agent.java.docker

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.BuildServiceAdapter
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ProgramCommandLine

class SystemJavaLocator(
    private val _buildStepContext: BuildStepContext,
) : ContainerJavaLocator {
    override fun asBuildService(onDiscovered: (String) -> Unit) =
        object : BuildServiceAdapter() {
            init {
                initialize(_buildStepContext.runnerContext.build, _buildStepContext.runnerContext)
            }

            override fun makeProgramCommandLine() =
                object : ProgramCommandLine {
                    override fun getExecutablePath() = "java"

                    override fun getWorkingDirectory() = _buildStepContext.runnerContext.workingDirectory.absolutePath

                    override fun getArguments() = listOf("-version")

                    override fun getEnvironment() = environmentVariables
                }

            override fun beforeProcessStarted() =
                logger.message("Running `java -version` inside container to check if it is configured in system PATH...")

            override fun getRunResult(exitCode: Int): BuildFinishedStatus {
                if (exitCode == 0) {
                    onDiscovered("java")
                    logger.message("Java is configured in container's PATH.")
                } else {
                    logger.message("Java was not found in container's PATH.")
                }
                return BuildFinishedStatus.FINISHED_SUCCESS
            }

            override fun isCommandLineLoggingEnabled() = false
        }
}
