package jetbrains.buildServer.agent.runner

data class StdOutProcessEvent(val stdOutText: String) : ProcessEvent(ProcessEventType.StdOut)