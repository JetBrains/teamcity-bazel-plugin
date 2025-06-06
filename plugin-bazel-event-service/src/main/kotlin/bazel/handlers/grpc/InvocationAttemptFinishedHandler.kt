package bazel.handlers.grpc

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.BuildStatusFormatter
import bazel.messages.Color
import bazel.messages.apply
import com.google.devtools.build.v1.BuildStatus

class InvocationAttemptFinishedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasInvocationAttemptFinished()) {
            return false
        }

        ctx.writer.flowFinished(ctx.streamId.invocationId)

        val invocationAttemptFinished = ctx.event.invocationAttemptFinished
        val status =
            if (invocationAttemptFinished.hasInvocationStatus()) {
                invocationAttemptFinished.invocationStatus.result
            } else {
                BuildStatus.Result.UNKNOWN_STATUS
            }

        if (status == BuildStatus.Result.COMMAND_SUCCEEDED) {
            if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                ctx.writer.message("Invocation attempt completed".apply(Color.Success))
            }
        } else {
            val description = BuildStatusFormatter.format(status)
            ctx.writer.error(
                buildString {
                    append("Invocation attempt failed")
                    if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                        append(": \"$description\"")
                    }
                },
                hasPrefix = false,
            )
        }

        return true
    }
}
