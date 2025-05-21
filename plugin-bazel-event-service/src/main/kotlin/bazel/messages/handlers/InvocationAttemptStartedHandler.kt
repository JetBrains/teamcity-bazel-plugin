package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.ServiceMessageContext

class InvocationAttemptStartedHandler : EventHandler {
    override fun handle(ctx: ServiceMessageContext): Boolean {
        if (!ctx.event.rawEvent.hasInvocationAttemptStarted()) {
            return false
        }
        val invocationAttemptStarted = ctx.event.rawEvent.invocationAttemptStarted
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

        val streamId = ctx.event.payload.streamId
        ctx.onNext(
            ctx.messageFactory.createFlowStarted(streamId.invocationId, streamId.buildId),
        )
        return true
    }
}
