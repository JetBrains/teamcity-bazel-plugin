package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext

class BuildEnqueuedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildEnqueued()) {
            return false
        }
        ctx.onNext(ctx.messageFactory.createMessage("Build enqueued"))
        return true
    }
}
