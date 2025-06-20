package bazel.messages

import com.google.devtools.build.v1.BuildStatus
import com.google.devtools.build.v1.BuildStatus.Result.*

object BuildStatusFormatter {
    fun format(source: BuildStatus.Result) =
        when (source) {
            COMMAND_SUCCEEDED -> "Build was successful and tests (if requested) all pass."
            COMMAND_FAILED -> "Build error and/or test failure."
            USER_ERROR -> "Unable to obtain a result due to input provided by the user."
            SYSTEM_ERROR -> "Unable to obtain a result due to a failure within the build system."
            RESOURCE_EXHAUSTED -> "Build required too many resources, such as build tool RAM."
            INVOCATION_DEADLINE_EXCEEDED -> "An invocation attempt time exceeded its deadline."
            REQUEST_DEADLINE_EXCEEDED -> "Build request time exceeded the request_deadline."
            CANCELLED -> "The build was cancelled by a call to CancelBuild."
            else -> "Unspecified or unknown"
        }
}
