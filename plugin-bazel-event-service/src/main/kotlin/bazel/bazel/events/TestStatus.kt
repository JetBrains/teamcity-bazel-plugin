

package bazel.bazel.events

import bazel.messages.Color

enum class TestStatus {
    NoStatus,
    Passed,
    Flaky,
    Timeout,
    Failed,
    Incomplete,
    RemoteFailure,
    FailedToBuild,
    ToolHaltedBeforeTesting,
}

fun TestStatus.toColor() =
    when (this) {
        TestStatus.Passed,
        -> Color.Success

        TestStatus.Flaky, TestStatus.Incomplete,
        -> Color.Warning

        TestStatus.Timeout,
        TestStatus.Failed,
        TestStatus.RemoteFailure,
        TestStatus.FailedToBuild,
        TestStatus.ToolHaltedBeforeTesting,
        -> Color.Error

        else -> Color.Default
    }
