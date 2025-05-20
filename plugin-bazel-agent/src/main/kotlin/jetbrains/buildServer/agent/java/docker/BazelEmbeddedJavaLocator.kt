package jetbrains.buildServer.agent.java.docker

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.BuildServiceAdapter
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.LoggingProcessListener
import jetbrains.buildServer.bazel.BazelCommandLineBuilder
import jetbrains.buildServer.bazel.commands.InfoJavaHomeCommand
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

// gets embedded into bazel dist java location using `bazel info java-home` command
class BazelEmbeddedJavaLocator(
    private val _buildStepContext: BuildStepContext,
    private val _commandLineBuilder: BazelCommandLineBuilder,
    private val _infoJavaCommand: InfoJavaHomeCommand,
) : ContainerJavaLocator {
    override fun asBuildService(onDiscovered: (String) -> Unit) =
        object : BuildServiceAdapter() {
            init {
                initialize(_buildStepContext.runnerContext.build, _buildStepContext.runnerContext)
            }

            private var javaFound = false

            override fun makeProgramCommandLine() = _commandLineBuilder.build(_infoJavaCommand)

            override fun beforeProcessStarted() = logger.message("Running `bazel info java-home` to locate embedded Java...")

            override fun getListeners() =
                listOf(
                    object : LoggingProcessListener(logger) {
                        override fun onStandardOutput(text: String) {
                            super.onStandardOutput(text)
                            onBazelStandardOutput(text)
                        }
                    },
                )

            private fun onBazelStandardOutput(text: String) {
                if (text.trimEnd().endsWith(BAZEL_JAVA_PATH_SUFFIX)) {
                    val javaExecutable =
                        Path(text)
                            .resolve("bin")
                            .resolve("java")
                            .absolutePathString()
                    onDiscovered(javaExecutable)
                    javaFound = true
                    logger.message("Found Bazel embedded Java at $javaExecutable")
                }
            }

            override fun afterProcessFinished() {
                if (!javaFound) {
                    logger.error("Bazel embedded Java was not found.")
                }
            }

            override fun getRunResult(exitCode: Int) = BuildFinishedStatus.FINISHED_SUCCESS

            override fun isCommandLineLoggingEnabled() = false
        }

    companion object {
        private const val BAZEL_JAVA_PATH_SUFFIX = "embedded_tools/jdk"
    }
}
