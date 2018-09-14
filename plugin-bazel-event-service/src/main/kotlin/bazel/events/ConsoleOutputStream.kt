package bazel.events

enum class ConsoleOutputStream(val description: String) {
    Unknown("Unspecified or unknown."),
    Stdout("Normal output stream."),
    Stderr("Error output stream.")
}