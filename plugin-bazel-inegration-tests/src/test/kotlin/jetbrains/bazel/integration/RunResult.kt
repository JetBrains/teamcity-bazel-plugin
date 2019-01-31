package jetbrains.bazel.integration

data class RunResult(
        val exitCode: Int = 0,
        val stdOut: List<String> = emptyList(),
        val stdErr: List<String> = emptyList(),
        val serviceMessages: List<ServiceMessage> = emptyList()) {
    override fun toString(): String {
        return "ExitCode: $exitCode\nstdOut:\n${stdOut.map { it.replace("##teamcity", "#@#teamcity") }.joinToString("\n")}\nstdErr:\n${stdErr.map { it.replace("##teamcity", "#@#teamcity") }.joinToString("\n")})"
    }
}