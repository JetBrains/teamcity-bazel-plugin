package bazel.handlers.grpc

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.MessageFactory
import bazel.messages.MessageFactory.createFlowStarted

class InvocationAttemptStartedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): HandlerResult {
        if (!ctx.event.hasInvocationAttemptStarted()) {
            return notHandled()
        }
        return handled(
            sequence {
                ctx.streamId.let {
                    yield(createFlowStarted(it.invocationId, it.buildId))
                }

                val invocationAttemptStarted = ctx.event.invocationAttemptStarted
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    yield(
                        MessageFactory.createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append("Invocation attempt #${invocationAttemptStarted.attemptNumber} started")
                            },
                        ),
                    )
                }
            },
        )
    }
}
