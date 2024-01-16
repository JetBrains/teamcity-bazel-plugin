

package jetbrains.buildServer.agent

data class CommandLineResult(val exitCode: Int = 0, val stdOut: String = "", val stdErr: String = "")