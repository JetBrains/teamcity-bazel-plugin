package jetbrains.buildServer.agent.java

import jetbrains.buildServer.agent.java.docker.BazelEmbeddedJavaLocator
import jetbrains.buildServer.agent.java.docker.SystemJavaLocator
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.bazel.CommandExecutionAdapter
import jetbrains.buildServer.messages.serviceMessages.BlockClosed
import jetbrains.buildServer.messages.serviceMessages.BlockOpened

class DockerJavaExecutableProvider(
    private val _buildStepContext: BuildStepContext,
    embeddedJavaLocator: BazelEmbeddedJavaLocator,
    systemJavaLocator: SystemJavaLocator,
) {
    private val logger get() = _buildStepContext.runnerContext.build.buildLogger
    private var javaExecutable: String? = null
    private val locators = listOf(systemJavaLocator, embeddedJavaLocator)

    fun getJavaExecutable(): String {
        if (javaExecutable == null) {
            logger.warning("Java location inside container is unknown, returning default value")
            return "java"
        }
        return javaExecutable!!
    }

    fun getCommandExecutionSequence(): Sequence<CommandExecutionAdapter> =
        sequence {
            disableOldDockerWrapperExtension()

            javaExecutable = null

            try {
                logger.message(BlockOpened(BLOCK_NAME).toString())

                for (locator in locators) {
                    yield(locator.asBuildService { javaExecutable = it })
                    if (javaExecutable != null) {
                        return@sequence
                    }
                }
            } finally {
                logger.message(BlockClosed(BLOCK_NAME).toString())
            }
        }.map { CommandExecutionAdapter(it) }

    private fun disableOldDockerWrapperExtension() {
        // by default docker wrapper plugin will reset executable to `java`
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
