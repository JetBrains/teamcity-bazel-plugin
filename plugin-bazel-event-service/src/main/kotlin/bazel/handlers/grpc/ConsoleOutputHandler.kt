package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.handlers.HandlerResult

class ConsoleOutputHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext) = HandlerResult(handled = ctx.event.hasConsoleOutput(), emptySequence())
}
