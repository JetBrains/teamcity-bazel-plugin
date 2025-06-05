package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.messages.MessageFactory.createErrorMessage

class NotProcessedEventHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): HandlerResult =
        handled(
            sequence {
                yield(createErrorMessage("Unknown event type: ${ctx.event}"))
            },
        )
}
