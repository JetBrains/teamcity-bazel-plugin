package jetbrains.buildServer.bazel

import jetbrains.buildServer.agent.runner.ProgramCommandLine

interface CommandLineBuilder {
    fun build(command: BazelCommand): ProgramCommandLine
}