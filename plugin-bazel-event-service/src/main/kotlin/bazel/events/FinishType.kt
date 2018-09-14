package bazel.events

enum class FinishType(val description: String) {
    Unspecified("Unknown or unspecified; callers should never set this value."),
    Finished ("Set by the event publisher to indicate a build event stream is finished."),
    Expired("Set by the WatchBuild RPC server when the publisher of a build event stream stops publishing events without publishing a BuildComponentStreamFinished event whose type equals FINISHED.")
}