package bazel.bazel.events

enum class AbortReason(val description: String) {
    Unknown("Unknown reason"),
    UserInterrupted("The user requested the build to be aborted (e.g., by hitting Ctl-C)"),
    NoAnalyze("The user requested that no analysis be performed"),
    NoBuild("The user requested that no build be carried out"),
    Timeout("The build or target was aborted as a timeout was exceeded"),
    RemoteEnvironmentFailure("The build or target was aborted as some remote environment (e.g., for remote execution of actions) was not available in the expected way"),
    Internal("Failure due to reasons entirely internal to the build tool, e.g., running out of memory"),
    LoadingFailure ("A Failure occurred in the loading phase of a target"),
    AnalysisFailure("A Failure occurred in the analysis phase of a target"),
    Skipped("Target build was skipped (e.g. due to incompatible CPU constraints)")
}