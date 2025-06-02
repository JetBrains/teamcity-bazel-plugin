package bazel.handlers.grpc

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.BuildStatusFormatter
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage
import com.google.devtools.build.v1.BuildStatus

class InvocationAttemptFinishedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasInvocationAttemptFinished()) {
            return false
        }
        ctx.onNext(ctx.messageFactory.createFlowFinished(ctx.streamId.invocationId))

        val invocationAttemptFinished = ctx.event.invocationAttemptFinished
        val status =
            if (invocationAttemptFinished.hasInvocationStatus()) {
                invocationAttemptFinished.invocationStatus.result
            } else {
                BuildStatus.Result.UNKNOWN_STATUS
            }
        if (status == BuildStatus.Result.COMMAND_SUCCEEDED) {
            if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("Invocation attempt completed".apply(Color.Success))
                            .toString(),
                    ),
                )
            }
        } else {
            val description = BuildStatusFormatter.format(status)
            ctx.onNext(
                ctx.messageFactory.createErrorMessage(
                    ctx
                        .buildMessage(false)
                        .append("Invocation attempt failed")
                        .append(": \"$description\"", Verbosity.Detailed)
                        .toString(),
                ),
            )
        }

        return true
    }
}
