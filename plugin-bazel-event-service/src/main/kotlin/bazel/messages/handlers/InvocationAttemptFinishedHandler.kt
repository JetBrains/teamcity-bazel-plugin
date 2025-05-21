package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.events.BuildStatus
import bazel.events.Result
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply
import bazel.v1.converters.BuildStatusConverter

class InvocationAttemptFinishedHandler : EventHandler {
    private val buildStatusConverter = BuildStatusConverter()
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.rawEvent.hasInvocationAttemptFinished()) {
            ctx.onNext(ctx.messageFactory.createFlowFinished(ctx.event.payload.streamId.invocationId))

            val invocationAttemptFinished = ctx.event.rawEvent.invocationAttemptFinished
            val result = if (invocationAttemptFinished.hasInvocationStatus()) buildStatusConverter.convert(
                invocationAttemptFinished.invocationStatus
            ) else Result.default
            if (result.status == BuildStatus.CommandSucceeded) {
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
                            .append(": \"${result.status.description}\"", Verbosity.Detailed)
                            .toString(),
                    ),
                )
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
}
