package bazel.messages.handlers

import bazel.bazel.events.TestStatus
import bazel.messages.Color

fun TestStatus.toColor() =
        when(this) {
            TestStatus.Passed
            -> Color.Success

            TestStatus.Flaky, TestStatus.Incomplete
            -> Color.Warning

            TestStatus.Timeout,
            TestStatus.Failed,
            TestStatus.RemoteFailure,
            TestStatus.FailedToBuild,
            TestStatus.ToolHaltedBeforeTesting
            -> Color.Error

            else -> Color.Default
        }