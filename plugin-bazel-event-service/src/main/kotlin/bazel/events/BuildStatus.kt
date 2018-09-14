package bazel.events

enum class BuildStatus(val description: String) {
    Unknown("Unspecified or unknown"),
    CommandSucceeded("Build was successful and tests (if requested) all pass."),
    CommandFailed("Build error and/or test failure."),
    UserError("Unable to obtain a result due to input provided by the user."),
    SystemError("Unable to obtain a result due to a failure within the build system."),
    ResourceExhausted("Build required too many resources, such as build tool RAM."),
    InvocationDeadlineExceeded("An invocation attempt time exceeded its deadline."),
    RequestDeadlineExceeded("Build request time exceeded the request_deadline."),
    Cancelled("The build was cancelled by a call to CancelBuild.")
}