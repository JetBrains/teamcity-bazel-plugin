package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import java.util.logging.Level
import java.util.logging.Logger

class NotProcessedEventHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        logger.log(Level.SEVERE, "Unknown event type: ${ctx.event}")
        return false
    }

    companion object {
        private val logger = Logger.getLogger(NotProcessedEventHandler::class.java.name)
    }
}
