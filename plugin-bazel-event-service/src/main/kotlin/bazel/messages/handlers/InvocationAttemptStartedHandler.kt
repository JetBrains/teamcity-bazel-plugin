

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.events.InvocationAttemptStarted
import bazel.messages.ServiceMessageContext

class InvocationAttemptStartedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is InvocationAttemptStarted) {
            val invocationAttemptStarted = ctx.event.payload as InvocationAttemptStarted
            if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("Invocation attempt #${invocationAttemptStarted.attemptNumber} started")
                            .toString(),
                    ),
                )
            }

            ctx.onNext(
                ctx.messageFactory.createFlowStarted(
                    invocationAttemptStarted.streamId.invocationId,
                    invocationAttemptStarted.streamId.buildId,
                ),
            )
            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
}
