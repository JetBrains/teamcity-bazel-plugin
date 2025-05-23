

package bazel.messages.handlers

import bazel.messages.BuildEventHandlerContext
import java.util.logging.Level
import java.util.logging.Logger

class NotProcessedEventHandler : EventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        logger.log(Level.SEVERE, "Unknown event type: ${ctx.event}")
        return false
    }

    companion object {
        private val logger = Logger.getLogger(NotProcessedEventHandler::class.java.name)
    }
}
