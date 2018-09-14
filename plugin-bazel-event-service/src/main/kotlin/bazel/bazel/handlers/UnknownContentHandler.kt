package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.BazelContent
import bazel.bazel.events.BazelUnknownContent
import java.util.logging.Level
import java.util.logging.Logger

class UnknownContentHandler : BazelHandler {
    override val priority = HandlerPriority.Last

    override fun handle(ctx: HandlerContext): BazelContent {
        logger.log(Level.SEVERE, "Unknown bazel event type: ${ctx.event}")
        return BazelUnknownContent(ctx.id, ctx.children)
    }

    companion object {
        private val logger = Logger.getLogger(UnknownContentHandler::class.java.name)
    }
}