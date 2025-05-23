package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.Color
import bazel.messages.HandlerContext
import bazel.messages.apply
import com.google.devtools.build.v1.StreamId
import com.google.devtools.build.v1.StreamId.BuildComponent.*

class MessageBuilder(
    private val context: HandlerContext,
) {
    private val text = StringBuilder()

    fun append(
        text: String,
        verbosity: Verbosity = context.verbosity,
        condition: () -> Boolean = { true },
    ): MessageBuilder {
        if (condition() && context.verbosity.atLeast(verbosity)) {
            this.text.append(text)
        }

        return this
    }

    fun appendPrefix() {
        if (context.verbosity.atLeast(Verbosity.Diagnostic)) {
            val message =
                buildString {
                    append("%8d".format(context.sequenceNumber))
                    append(' ')

                    context.streamId?.let { streamId ->
                        append(formatComponent(streamId.component))
                        append(' ')
                        append(streamId.buildId.take(8))
                        if (streamId.invocationId.isNotEmpty()) {
                            append(':')
                            append(streamId.invocationId.take(8))
                        }
                    }

                    append(' ')
                }

            this.text.append(message.apply(Color.Trace))
        }
    }

    private fun formatComponent(component: StreamId.BuildComponent) =
        when (component) {
            CONTROLLER -> "Controller"
            WORKER -> "Worker"
            TOOL -> "Tool"
            else -> "UnknownComponent"
        }

    override fun toString(): String = text.toString()
}

fun HandlerContext.buildMessage(improve: Boolean = true): MessageBuilder = MessageBuilder(this).also { if (improve) it.appendPrefix() }
