package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.MessageFactory

class BuildEnqueuedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildEnqueued()) {
            return false
        }
        ctx.emitMessage(MessageFactory.createMessage("Build enqueued"))
        return true
    }
}
