package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class MessageBuilder(
        private val _serviceMessageContext: ServiceMessageContext,
        improve: Boolean) {

    private val _text = StringBuilder()

    init {
        if (improve && _serviceMessageContext.verbosity.atLeast(Verbosity.Trace)) {
            val text = StringBuilder()
            text.append(String.format("%8d", _serviceMessageContext.event.payload.sequenceNumber))
            text.append(' ')
            text.append(_serviceMessageContext.event.payload.streamId.component)
            text.append(' ')
            val streamId = _serviceMessageContext.event.payload.streamId
            text.append(if (streamId.invocationId.isNotEmpty()) "${streamId.buildId.take(8)}:${streamId.invocationId.take(8)}" else streamId.buildId.take(8))
            text.append(' ')
            _text.append(text.toString().apply(Color.Trace))
        }
    }

    fun append(text: String, verbosity: Verbosity = _serviceMessageContext.verbosity): MessageBuilder {
        if(_serviceMessageContext.verbosity.atLeast(verbosity)) {
            this._text.append(text)
        }

        return this
    }

    override fun toString(): String {
        return _text.toString()
    }
}

fun ServiceMessageContext.buildMessage(improve: Boolean = true): MessageBuilder {
    return MessageBuilder(this, improve)
}