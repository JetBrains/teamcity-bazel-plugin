package bazel.handlers.bes

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BesEventHandler
import bazel.handlers.BesEventHandlerContext
import bazel.messages.buildMessage

class InvocationAttemptStartedHandler : BesEventHandler {
    override fun handle(ctx: BesEventHandlerContext): Boolean {
        if (!ctx.event.hasInvocationAttemptStarted()) {
            return false
        }
        val invocationAttemptStarted = ctx.event.invocationAttemptStarted
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
            ctx.messageFactory.createFlowStarted(ctx.streamId.invocationId, ctx.streamId.buildId),
        )
        return true
    }
}
