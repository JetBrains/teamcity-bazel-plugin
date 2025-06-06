package bazel.messages

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.MessageFactory.createErrorMessage
import bazel.messages.MessageFactory.createFlowFinished
import bazel.messages.MessageFactory.createFlowStarted
import bazel.messages.MessageFactory.createMessage
import bazel.messages.MessageFactory.createTraceMessage
import com.google.devtools.build.v1.StreamId
import com.google.devtools.build.v1.StreamId.BuildComponent.CONTROLLER
import com.google.devtools.build.v1.StreamId.BuildComponent.TOOL
import com.google.devtools.build.v1.StreamId.BuildComponent.WORKER
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class MessageWriter(
    private val verbosity: Verbosity,
    private val sequenceNumber: Long,
    private val streamId: StreamId? = null,
    private val output: (ServiceMessage) -> Unit = { println(it.asString()) },
) {
    fun message(
        text: String,
        hasPrefix: Boolean = true,
    ) = output(createMessage(format(text, hasPrefix)))

    fun error(
        text: String,
        hasPrefix: Boolean = true,
    ) = output(createErrorMessage(format(text, hasPrefix)))

    fun trace(text: String) = output(createTraceMessage(text))

    fun flowStarted(
        flowId: String,
        parentFlowId: String,
    ) = output(createFlowStarted(flowId, parentFlowId))

    fun flowFinished(flowId: String) = output(createFlowFinished(flowId))

    private fun format(
        text: String,
        hasPrefix: Boolean,
    ) = if (hasPrefix) "$messagePrefix$text" else text

    private val messagePrefix: String
        get() {
            fun formatComponent(component: StreamId.BuildComponent) =
                when (component) {
                    CONTROLLER -> "Controller"
                    WORKER -> "Worker"
                    TOOL -> "Tool"
                    else -> "UnknownComponent"
                }

            if (!verbosity.atLeast(Verbosity.Diagnostic)) return ""
            return buildString {
                append("%8d".format(sequenceNumber))
                append(' ')

                streamId?.let { streamId ->
                    append(formatComponent(streamId.component))
                    append(' ')
                    append(streamId.buildId.take(8))
                    if (streamId.invocationId.isNotEmpty()) {
                        append(':')
                        append(streamId.invocationId.take(8))
                    }
                }

                append(' ')
            }.apply(Color.Trace)
        }
}
