package bazel.handlers.grpc

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.MessageFactory
import bazel.messages.buildMessage

class InvocationAttemptStartedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasInvocationAttemptStarted()) {
            return false
        }
        val invocationAttemptStarted = ctx.event.invocationAttemptStarted
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.onNext(
                MessageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("Invocation attempt #${invocationAttemptStarted.attemptNumber} started")
                        .toString(),
                ),
            )
        }

        ctx.onNext(
            MessageFactory.createFlowStarted(ctx.streamId.invocationId, ctx.streamId.buildId),
        )
        return true
    }
}
