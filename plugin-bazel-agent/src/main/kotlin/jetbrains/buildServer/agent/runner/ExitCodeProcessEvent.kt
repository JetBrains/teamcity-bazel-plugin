package jetbrains.buildServer.agent.runner

data class ExitCodeProcessEvent(val exitCode: Int) : ProcessEvent(ProcessEventType.ExitCode)