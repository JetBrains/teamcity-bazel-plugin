

package bazel.messages

import jetbrains.buildServer.messages.serviceMessages.MessageWithAttributes
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes

class Message(text: String, status: String, errDetails: String? = null)
    : MessageWithAttributes(
        ServiceMessageTypes.MESSAGE,
        mapOf(
                "text" to text,
                "status" to status,
                "errorDetails" to errDetails,
                "tc:tags" to "tc:parseServiceMessagesInside")
                .filter { it.value != null })