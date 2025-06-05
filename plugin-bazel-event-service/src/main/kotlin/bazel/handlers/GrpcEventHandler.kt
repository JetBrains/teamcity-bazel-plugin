package bazel.handlers

import bazel.Verbosity
import com.google.devtools.build.v1.BuildEvent
import com.google.devtools.build.v1.StreamId

interface GrpcEventHandler {
    fun handle(ctx: GrpcEventHandlerContext): HandlerResult
}

data class GrpcEventHandlerContext(
    val verbosity: Verbosity,
    val streamId: StreamId,
    val messagePrefix: String,
    val event: BuildEvent,
)
