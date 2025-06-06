package bazel.handlers

import bazel.Verbosity
import bazel.messages.MessageWriter
import com.google.devtools.build.v1.BuildEvent
import com.google.devtools.build.v1.StreamId

interface GrpcEventHandler {
    fun handle(ctx: GrpcEventHandlerContext): Boolean
}

data class GrpcEventHandlerContext(
    val verbosity: Verbosity,
    val streamId: StreamId,
    val event: BuildEvent,
    val writer: MessageWriter,
)
