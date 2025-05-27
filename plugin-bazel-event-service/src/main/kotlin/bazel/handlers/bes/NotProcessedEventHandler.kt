package bazel.handlers.bes

import bazel.handlers.BesEventHandler
import bazel.handlers.BesEventHandlerContext
import java.util.logging.Level
import java.util.logging.Logger

class NotProcessedEventHandler : BesEventHandler {
    override fun handle(ctx: BesEventHandlerContext): Boolean {
        logger.log(Level.SEVERE, "Unknown event type: ${ctx.event}")
        return false
    }

    companion object {
        private val logger = Logger.getLogger(NotProcessedEventHandler::class.java.name)
    }
}
