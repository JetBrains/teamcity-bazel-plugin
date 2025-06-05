package bazel.handlers

import bazel.Verbosity
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

interface BuildEventHandler {
    fun handle(ctx: BuildEventHandlerContext): HandlerResult
}

data class BuildEventHandlerContext(
    val verbosity: Verbosity,
    val messagePrefix: String,
    val event: BuildEventStreamProtos.BuildEvent,
) {
    companion object {
        fun fromBesContext(
            ctx: GrpcEventHandlerContext,
            event: BuildEventStreamProtos.BuildEvent,
        ) = BuildEventHandlerContext(
            ctx.verbosity,
            ctx.messagePrefix,
            event,
        )
    }
}
