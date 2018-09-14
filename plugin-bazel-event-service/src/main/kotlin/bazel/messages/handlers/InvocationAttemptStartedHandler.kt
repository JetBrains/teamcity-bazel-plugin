package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.events.InvocationAttemptStarted
import bazel.messages.ServiceMessageContext

class InvocationAttemptStartedHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is InvocationAttemptStarted) {
            if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                ctx.onNext(ctx.messageFactory.createMessage(
                        ctx.buildMessage()
                                .append("Invocation attempt #${ctx.event.payload.attemptNumber} started")
                                .toString()))
            }

            ctx.onNext(ctx.messageFactory.createFlowStarted(ctx.event.payload.streamId.invocationId, ctx.event.payload.streamId.buildId))
            true
        } else ctx.handlerIterator.next().handle(ctx)
}