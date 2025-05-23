package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.events.BuildStatus
import bazel.messages.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.v1.converters.BuildStatusConverter

class InvocationAttemptFinishedHandler : EventHandler {
    private val buildStatusConverter = BuildStatusConverter()

    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasInvocationAttemptFinished()) {
            return false
        }
        ctx.onNext(ctx.messageFactory.createFlowFinished(ctx.streamId.invocationId))

        val invocationAttemptFinished = ctx.event.invocationAttemptFinished
        val status =
            if (invocationAttemptFinished.hasInvocationStatus()) {
                buildStatusConverter.convert(
                    invocationAttemptFinished.invocationStatus,
                )
            } else {
                BuildStatus.Unknown
            }
        if (status == BuildStatus.CommandSucceeded) {
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
            ctx.onNext(
                ctx.messageFactory.createErrorMessage(
                    ctx
                        .buildMessage(false)
                        .append("Invocation attempt failed")
                        .append(": \"${status.description}\"", Verbosity.Detailed)
                        .toString(),
                ),
            )
        }

        return true
    }
}
