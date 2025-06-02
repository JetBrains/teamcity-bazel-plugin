package bazel.handlers.grpc

import bazel.handlers.BepEventHandlerChain
import bazel.handlers.BepEventHandlerContext
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.Hierarchy
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import java.util.logging.Level
import java.util.logging.Logger

class PackedBazelEventHandler(
    val bazelEventHandler: BepEventHandlerChain,
    val hierarchy: Hierarchy,
) : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
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
        val ctx = BepEventHandlerContext.Companion.fromBesContext(ctx, hierarchy, event)
        return bazelEventHandler.handle(ctx)
    }

    companion object {
        private val logger = Logger.getLogger(PackedBazelEventHandler::class.java.name)
    }
}
