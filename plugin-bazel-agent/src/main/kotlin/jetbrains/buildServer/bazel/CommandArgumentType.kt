package jetbrains.buildServer.bazel

enum class CommandArgumentType {
    StartupOption,
    Command,
    Argument,
    Target,
}