package bazel.messages.handlers

import bazel.Event
import bazel.Verbosity
import bazel.atLeast
import bazel.events.BuildComponent
import bazel.events.OrderedBuildEvent
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply

interface MessageBuilderContext {
    val messageFactory: MessageFactory
    val verbosity: Verbosity
    val event: Event<OrderedBuildEvent>
}

class MessageBuilder(
    private val serviceMessageContext: MessageBuilderContext,
) {
    private val text = StringBuilder()

    fun append(
        text: String,
        verbosity: Verbosity = serviceMessageContext.verbosity,
        condition: () -> Boolean = { true },
    ): MessageBuilder {
        if (condition() && serviceMessageContext.verbosity.atLeast(verbosity)) {
            this.text.append(text)
        }

        return this
    }

    fun appendPrefix() {
        if (serviceMessageContext.verbosity.atLeast(Verbosity.Diagnostic)) {
            val payload = serviceMessageContext.event.payload
            val streamId = payload.streamId

            val message =
                buildString {
                    append("%8d".format(payload.sequenceNumber))
                    append(' ')
                    if (streamId.component != BuildComponent.UnknownComponent) {
                        append(streamId.component)
                        append(' ')
                    }
                    append(streamId.buildId.take(8))
                    if (streamId.invocationId.isNotEmpty()) {
                        append(':')
                        append(streamId.invocationId.take(8))
                    }
                    append(' ')
                }

            this.text.append(message.apply(Color.Trace))
        }
    }

    override fun toString(): String = text.toString()
}

fun MessageBuilderContext.buildMessage(improve: Boolean = true): MessageBuilder =
    MessageBuilder(this).also { if (improve) it.appendPrefix() }
