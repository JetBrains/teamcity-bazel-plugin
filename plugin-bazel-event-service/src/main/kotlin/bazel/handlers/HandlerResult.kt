package bazel.handlers

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class HandlerResult(
    val handled: Boolean,
    val messages: Sequence<ServiceMessage>,
) {
    companion object {
        fun handled(messages: Sequence<ServiceMessage>) = HandlerResult(true, messages)

        fun handled() = HandlerResult(true, emptySequence())

        fun notHandled() = HandlerResult(false, emptySequence())
    }
}
