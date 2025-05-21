

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.messages.ServiceMessageContext
import java.util.logging.Level
import java.util.logging.Logger

class NotProcessedEventHandler : EventHandler {
    override val priority: HandlerPriority get() = HandlerPriority.Last

    override fun handle(ctx: ServiceMessageContext): Boolean {
        logger.log(Level.SEVERE, "Unknown event type: ${ctx.event}")
        return false
    }

    companion object {
        private val logger = Logger.getLogger(NotProcessedEventHandler::class.java.name)
    }
}
