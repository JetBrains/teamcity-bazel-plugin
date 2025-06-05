package bazel.handlers.grpc

import bazel.handlers.BuildEventHandlerChain
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.MessageFactory.createErrorMessage
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class PackedBazelEventHandler(
    val bazelEventHandler: BuildEventHandlerChain,
) : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): HandlerResult {
        if (!ctx.event.hasBazelEvent()) {
            return notHandled()
        }

        val bazelEvent = ctx.event.bazelEvent
        val bazelEventType = bazelEvent.typeUrl
        if (bazelEventType != "type.googleapis.com/build_event_stream.BuildEvent") {
            return handled(
                sequence {
                    yield(createErrorMessage("Unknown bazel event type: $bazelEventType"))
                },
            )
        }
        val event = bazelEvent.unpack(BuildEventStreamProtos.BuildEvent::class.java)
        val ctx = BuildEventHandlerContext.fromBesContext(ctx, event)
        return bazelEventHandler.handle(ctx)
    }
}
