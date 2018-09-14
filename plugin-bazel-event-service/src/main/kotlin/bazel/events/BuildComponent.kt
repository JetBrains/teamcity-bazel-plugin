package bazel.events

enum class BuildComponent(val description: String) {
    UnknownComponent("Unknown or unspecified; callers should never set this value."),
    Controller("A component that coordinates builds."),
    Worker("A component that runs executables needed to complete a build."),
    Tool("A component that builds something.")
}