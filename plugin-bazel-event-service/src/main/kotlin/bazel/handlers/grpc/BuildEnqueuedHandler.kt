package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.MessageFactory

class BuildEnqueuedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): HandlerResult {
        if (!ctx.event.hasBuildEnqueued()) {
            return notHandled()
        }
        return handled(
            sequence {
                yield(MessageFactory.createMessage("Build enqueued"))
            },
        )
    }
}
