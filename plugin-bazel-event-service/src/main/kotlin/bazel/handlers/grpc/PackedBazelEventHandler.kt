package bazel.handlers.grpc

import bazel.handlers.BuildEventHandlerChain
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class PackedBazelEventHandler(
    val bazelEventHandler: BuildEventHandlerChain,
) : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasBazelEvent()) {
            return false
        }

        val bazelEvent = ctx.event.bazelEvent
        val bazelEventType = bazelEvent.typeUrl
        if (bazelEventType != "type.googleapis.com/build_event_stream.BuildEvent") {
            ctx.writer.error("Unknown bazel event type: $bazelEventType")
            return true
        }

        val event = bazelEvent.unpack(BuildEventStreamProtos.BuildEvent::class.java)
        val ctx = BuildEventHandlerContext.fromBesContext(ctx, event)
        bazelEventHandler.handle(ctx)
        return true
    }
}
