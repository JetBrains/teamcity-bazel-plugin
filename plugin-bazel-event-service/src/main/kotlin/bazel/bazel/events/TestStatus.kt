package bazel.bazel.events

enum class TestStatus {
    NoStatus,
    Passed,
    Flaky,
    Timeout,
    Failed,
    Incomplete,
    RemoteFailure,
    FailedToBuild,
    ToolHaltedBeforeTesting
}