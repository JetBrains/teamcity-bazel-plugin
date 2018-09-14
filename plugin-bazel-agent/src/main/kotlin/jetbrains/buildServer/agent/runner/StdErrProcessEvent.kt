package jetbrains.buildServer.agent.runner

data class StdErrProcessEvent(val stdErrText: String) : ProcessEvent(ProcessEventType.StdErr)