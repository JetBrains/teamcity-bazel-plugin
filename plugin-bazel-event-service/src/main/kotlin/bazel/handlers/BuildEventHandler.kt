package bazel.handlers

import bazel.Verbosity
import bazel.messages.Hierarchy
import bazel.messages.MessageBuilderContext
import bazel.messages.MessageFactory
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
    val messageFactory: MessageFactory,
    val hierarchy: Hierarchy,
    val event: BuildEventStreamProtos.BuildEvent,
    val onNext: (ServiceMessage) -> Unit,
) : MessageBuilderContext {
    companion object {
        fun fromBesContext(
            ctx: GrpcEventHandlerContext,
            hierarchy: Hierarchy,
            event: BuildEventStreamProtos.BuildEvent,
        ) = BuildEventHandlerContext(
            ctx.verbosity,
            ctx.sequenceNumber,
            ctx.streamId,
            ctx.messageFactory,
            hierarchy,
            event,
            ctx.onNext,
        )
    }
}
