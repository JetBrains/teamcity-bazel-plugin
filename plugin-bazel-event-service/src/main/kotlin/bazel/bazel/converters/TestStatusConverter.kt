

package bazel.bazel.converters

import bazel.bazel.events.TestStatus
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

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
