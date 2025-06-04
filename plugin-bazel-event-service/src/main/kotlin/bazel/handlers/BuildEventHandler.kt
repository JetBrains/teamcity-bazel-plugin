package bazel.handlers

import bazel.Verbosity
import bazel.messages.MessageBuilderContext
import bazel.messages.TargetRegistry
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.devtools.build.v1.StreamId
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

interface BuildEventHandler {
    fun handle(ctx: BuildEventHandlerContext): Boolean
}

data class BuildEventHandlerContext(
    override val verbosity: Verbosity,
    override val sequenceNumber: Long,
    override val streamId: StreamId? = null,
    val targetRegistry: TargetRegistry,
    val event: BuildEventStreamProtos.BuildEvent,
    val emitMessage: (ServiceMessage) -> Unit,
) : MessageBuilderContext {
    companion object {
        fun fromBesContext(
            ctx: GrpcEventHandlerContext,
            targetRegistry: TargetRegistry,
            event: BuildEventStreamProtos.BuildEvent,
        ) = BuildEventHandlerContext(
            ctx.verbosity,
            ctx.sequenceNumber,
            ctx.streamId,
            targetRegistry,
            event,
            ctx.emitMessage,
        )
    }
}
