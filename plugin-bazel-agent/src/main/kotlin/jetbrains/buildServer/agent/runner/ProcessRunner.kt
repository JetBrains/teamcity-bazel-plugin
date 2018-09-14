package jetbrains.buildServer.agent.runner

import devteam.rx.Observable

interface ProcessRunner {
    fun run(title: String, idleTimeoutSeconds: Int, commandLine: ProgramCommandLine): Observable<ProcessEvent>
}