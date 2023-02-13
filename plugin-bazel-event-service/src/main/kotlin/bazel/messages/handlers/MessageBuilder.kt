/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        if (improve && _serviceMessageContext.verbosity.atLeast(Verbosity.Diagnostic)) {
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

    fun append(text: String, verbosity: Verbosity = _serviceMessageContext.verbosity, condition: () -> Boolean = { true }): MessageBuilder {
        if (condition() && _serviceMessageContext.verbosity.atLeast(verbosity)) {
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