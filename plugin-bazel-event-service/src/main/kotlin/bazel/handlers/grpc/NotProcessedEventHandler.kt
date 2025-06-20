package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class NotProcessedEventHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        ctx.writer.error("Unknown event: ${ctx.event}".apply(Color.Warning))
        return true
    }
}
