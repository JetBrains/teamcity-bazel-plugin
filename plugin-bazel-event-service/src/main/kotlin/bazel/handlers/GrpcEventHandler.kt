package bazel.handlers

import bazel.Verbosity
import bazel.messages.MessageBuilderContext
import bazel.messages.MessageFactory
import com.google.devtools.build.v1.BuildEvent
import com.google.devtools.build.v1.StreamId
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

interface GrpcEventHandler {
    fun handle(ctx: GrpcEventHandlerContext): Boolean
}

data class GrpcEventHandlerContext(
    override val verbosity: Verbosity,
    override val sequenceNumber: Long,
    override val streamId: StreamId,
    val messageFactory: MessageFactory,
    val event: BuildEvent,
    val onNext: (ServiceMessage) -> Unit,
) : MessageBuilderContext
