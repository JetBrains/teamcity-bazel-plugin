

package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class MessageBuilder(
    private val serviceMessageContext: ServiceMessageContext,
    improve: Boolean,
) {
    private val text = StringBuilder()

    init {
        if (improve && serviceMessageContext.verbosity.atLeast(Verbosity.Diagnostic)) {
            val text = StringBuilder()
            text.append(String.format("%8d", serviceMessageContext.event.payload.sequenceNumber))
            text.append(' ')
            text.append(serviceMessageContext.event.payload.streamId.component)
            text.append(' ')
            val streamId = serviceMessageContext.event.payload.streamId
            text.append(
                if (streamId.invocationId.isNotEmpty()) {
                    "${streamId.buildId.take(
                        8,
                    )}:${streamId.invocationId.take(8)}"
                } else {
                    streamId.buildId.take(8)
                },
            )
            text.append(' ')
            text.append(text.toString().apply(Color.Trace))
        }
    }

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

    override fun toString(): String = text.toString()
}

fun ServiceMessageContext.buildMessage(improve: Boolean = true): MessageBuilder = MessageBuilder(this, improve)
