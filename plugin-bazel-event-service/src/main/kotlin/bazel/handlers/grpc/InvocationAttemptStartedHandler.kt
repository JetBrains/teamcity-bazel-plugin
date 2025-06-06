package bazel.handlers.grpc

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext

class InvocationAttemptStartedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasInvocationAttemptStarted()) {
            return false
        }

        ctx.writer.flowStarted(ctx.streamId.invocationId, ctx.streamId.buildId)

        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            val invocationAttemptStarted = ctx.event.invocationAttemptStarted
            ctx.writer.message("Invocation attempt #${invocationAttemptStarted.attemptNumber} started")
        }

        return true
    }
}
