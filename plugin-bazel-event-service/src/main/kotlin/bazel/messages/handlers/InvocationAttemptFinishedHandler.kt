package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import com.google.devtools.build.v1.BuildStatus

class InvocationAttemptFinishedHandler : EventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
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
