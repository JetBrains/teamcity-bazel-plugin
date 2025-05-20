package jetbrains.buildServer.agent.java.docker

import jetbrains.buildServer.agent.runner.BuildServiceAdapter

interface ContainerJavaLocator {
    fun asBuildService(onDiscovered: (String) -> Unit): BuildServiceAdapter
}
