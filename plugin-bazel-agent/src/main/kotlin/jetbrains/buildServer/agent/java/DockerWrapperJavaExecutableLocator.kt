package jetbrains.buildServer.agent.java

import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.bazel.CommandExecutionAdapter
import jetbrains.buildServer.messages.serviceMessages.BlockClosed
import jetbrains.buildServer.messages.serviceMessages.BlockOpened

class DockerWrapperJavaExecutableLocator(
    private val _buildStepContext: BuildStepContext,
    private val _bazelEmbeddedJava: BazelEmbeddedJavaLocator,
    private val _systemJava: SystemJavaLocator,
    private val _javaExecutableProvider: JavaExecutableProvider,
) {
    private val logger get() = _buildStepContext.runnerContext.build.buildLogger

    fun getCommandExecutionSequence(): Sequence<CommandExecutionAdapter> =
        sequence {
            if (!_buildStepContext.runnerContext.isVirtualContext) {
                return@sequence
            }

            // previously docker-plugin was responsible for path resolution inside docker
            disableOldDockerWrapperExtension()

            try {
                logger.message(BlockOpened(BLOCK_NAME).toString())
                // checking if java exists in PATH using `java -version` in container
                _systemJava.let {
                    yield(it.asBuildService())
                    if (it.discoveredPath != null) {
                        _javaExecutableProvider.containerJavaExecutable = it.discoveredPath
                        return@sequence
                    }
                }

                // system java not found, getting embedded into bazel java using `bazel info java-home` command
                _bazelEmbeddedJava.let {
                    yield(it.asBuildService())
                    if (it.discoveredPath != null) {
                        _javaExecutableProvider.containerJavaExecutable = it.discoveredPath
                    }
                }
            } finally {
                logger.message(BlockClosed(BLOCK_NAME).toString())
            }
        }.map { CommandExecutionAdapter(it) }

    private fun disableOldDockerWrapperExtension() {
        _buildStepContext.runnerContext.addConfigParameter(
            DOCKER_OLD_EXTENSION_FEATURE_FLAG,
            "false",
        )
    }

    companion object {
        private const val DOCKER_OLD_EXTENSION_FEATURE_FLAG =
            "teamcity.internal.docker.bazelCommandLineExtension.enabled"
        private const val BLOCK_NAME = "Locating java inside container"
    }
}
