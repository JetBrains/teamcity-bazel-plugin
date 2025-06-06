package bazel.handlers

import bazel.Verbosity
import bazel.messages.MessageWriter
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

interface BuildEventHandler {
    fun handle(ctx: BuildEventHandlerContext): Boolean
}

data class BuildEventHandlerContext(
    val verbosity: Verbosity,
    val event: BuildEventStreamProtos.BuildEvent,
    val writer: MessageWriter,
) {
    companion object {
        fun fromBesContext(
            ctx: GrpcEventHandlerContext,
            event: BuildEventStreamProtos.BuildEvent,
        ) = BuildEventHandlerContext(
            ctx.verbosity,
            event,
            ctx.writer,
        )
    }
}
