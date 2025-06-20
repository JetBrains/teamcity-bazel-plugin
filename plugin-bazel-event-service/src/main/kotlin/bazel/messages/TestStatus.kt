package bazel.messages

import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

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

class TestStatusConverter {
    fun convert(source: BuildEventStreamProtos.TestStatus) =
        when (source.number) {
            1 -> TestStatus.Passed
            2 -> TestStatus.Flaky
            3 -> TestStatus.Timeout
            4 -> TestStatus.Failed
            5 -> TestStatus.Incomplete
            6 -> TestStatus.RemoteFailure
            7 -> TestStatus.FailedToBuild
            8 -> TestStatus.ToolHaltedBeforeTesting
            else -> TestStatus.NoStatus
        }
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
