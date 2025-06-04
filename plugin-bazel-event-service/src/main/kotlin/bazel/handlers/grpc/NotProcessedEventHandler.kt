package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.MessageFactory

class NotProcessedEventHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        ctx.emitMessage(MessageFactory.createErrorMessage("Unknown event type: ${ctx.event}"))
        return false
    }
}
