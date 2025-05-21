

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.events.BuildStatus
import bazel.events.InvocationAttemptFinished
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class InvocationAttemptFinishedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is InvocationAttemptFinished) {
            ctx.onNext(ctx.messageFactory.createFlowFinished(ctx.event.payload.streamId.invocationId))

            val invocationAttemptFinished = ctx.event.payload as InvocationAttemptFinished
            if (invocationAttemptFinished.invocationResult.status == BuildStatus.CommandSucceeded) {
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
                            .append(": \"${invocationAttemptFinished.invocationResult.status.description}\"", Verbosity.Detailed)
                            .toString(),
                    ),
                )
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
}
