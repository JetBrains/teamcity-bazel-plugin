package bazel.messages.handlers

import bazel.bazel.events.Id
import bazel.messages.BazelEventHandlerContext
import bazel.messages.BuildEventHandlerContext
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import java.util.logging.Level
import java.util.logging.Logger

class PackedBazelEventHandler(
    val bazelEventHandler: RootBazelEventHandler,
) : EventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasBazelEvent()) {
            return false
        }

        val bazelEvent = ctx.event.bazelEvent
        val bazelEventType = bazelEvent.typeUrl
        if (bazelEventType != "type.googleapis.com/build_event_stream.BuildEvent") {
            logger.log(Level.SEVERE, "Unknown bazel event: $bazelEventType")
            return true
        }
        val event = bazelEvent.unpack(BuildEventStreamProtos.BuildEvent::class.java)
        val ctx = BazelEventHandlerContext.fromBuildContext(ctx, event)
        val handled = bazelEventHandler.handle(ctx)
        val id = Id(event.id)
        val children = event.childrenList.map { Id(it) }
        ctx.hierarchy.createNode(id, children, "")
        ctx.hierarchy.tryCloseNode(id)

        return handled
    }

    companion object {
        private val logger = Logger.getLogger(PackedBazelEventHandler::class.java.name)
    }
}
