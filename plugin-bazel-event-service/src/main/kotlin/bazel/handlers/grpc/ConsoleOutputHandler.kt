package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext

class ConsoleOutputHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext) = ctx.event.hasConsoleOutput()
}
