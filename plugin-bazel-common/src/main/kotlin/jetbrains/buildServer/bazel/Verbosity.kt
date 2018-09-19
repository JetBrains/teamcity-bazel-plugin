package jetbrains.buildServer.bazel

enum class Verbosity(val id: String, val description: String) {
    Quiet("Quiet", "Quiet"),
    Normal("Normal", "Normal"),
    Verbose("Verbose", "Verbose"),
    Detailed("Detailed", "Detailed"),
    Diagnostic("Diagnostic", "Diagnostic");

    companion object {
        fun tryParse(id: String): Verbosity? {
            return Verbosity.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}