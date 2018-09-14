package bazel.v1.handlers

import bazel.HandlerPriority
import bazel.events.OrderedBuildEvent
import bazel.events.UnknownEvent
import java.util.logging.Level
import java.util.logging.Logger

class UnknownEventHandler: EventHandler {
    override val priority: HandlerPriority = HandlerPriority.Last

    override fun handle(ctx: HandlerContext): OrderedBuildEvent {
        logger.log(Level.SEVERE, "Unknown event type: ${ctx.event}")
        return UnknownEvent(ctx.streamId)
    }

    companion object {
        private val logger = Logger.getLogger(UnknownEventHandler::class.java.name)
    }
}