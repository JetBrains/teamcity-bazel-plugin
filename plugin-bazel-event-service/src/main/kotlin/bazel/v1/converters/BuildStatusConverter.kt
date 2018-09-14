package bazel.v1.converters

import bazel.Converter
import bazel.events.Result
import com.google.devtools.build.v1.BuildStatus

class BuildStatusConverter: Converter<BuildStatus, Result> {
    override fun convert(source: com.google.devtools.build.v1.BuildStatus) =
            Result(
                    when (source.resultValue) {
                        1 -> bazel.events.BuildStatus.CommandSucceeded
                        2 -> bazel.events.BuildStatus.CommandFailed
                        3 -> bazel.events.BuildStatus.UserError
                        4 -> bazel.events.BuildStatus.SystemError
                        5 -> bazel.events.BuildStatus.ResourceExhausted
                        6 -> bazel.events.BuildStatus.InvocationDeadlineExceeded
                        8 -> bazel.events.BuildStatus.RequestDeadlineExceeded
                        7 -> bazel.events.BuildStatus.Cancelled
                        else -> bazel.events.BuildStatus.Unknown
                    }
            )
}