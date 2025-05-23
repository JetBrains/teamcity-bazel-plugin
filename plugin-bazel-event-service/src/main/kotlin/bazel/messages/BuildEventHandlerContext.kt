package bazel.messages

import bazel.Verbosity
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.devtools.build.v1.BuildEvent
import com.google.devtools.build.v1.StreamId
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

interface HandlerContext {
    val messageFactory: MessageFactory
    val hierarchy: Hierarchy
    val verbosity: Verbosity
    val sequenceNumber: Long
    val projectId: String?
    val streamId: StreamId?
    val onNext: (ServiceMessage) -> Unit
}

data class BuildEventHandlerContext(
    override val messageFactory: MessageFactory,
    override val hierarchy: Hierarchy,
    override val verbosity: Verbosity,
    override val sequenceNumber: Long,
    override val projectId: String,
    override val streamId: StreamId,
    val event: BuildEvent,
    override val onNext: (ServiceMessage) -> Unit,
) : HandlerContext

data class BazelEventHandlerContext(
    override val messageFactory: MessageFactory,
    override val hierarchy: Hierarchy,
    override val verbosity: Verbosity,
    override val sequenceNumber: Long,
    override val projectId: String? = null,
    override val streamId: StreamId? = null,
    val bazelEvent: BuildEventStreamProtos.BuildEvent,
    override val onNext: (ServiceMessage) -> Unit,
) : HandlerContext {
    companion object {
        fun fromBuildContext(
            ctx: BuildEventHandlerContext,
            bazelEvent: BuildEventStreamProtos.BuildEvent,
        ) = BazelEventHandlerContext(
            ctx.messageFactory,
            ctx.hierarchy,
            ctx.verbosity,
            ctx.sequenceNumber,
            ctx.projectId,
            ctx.streamId,
            bazelEvent,
            ctx.onNext,
        )
    }
}
