package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BuildEventHandlerContext

class InvocationAttemptStartedHandler : EventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
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
