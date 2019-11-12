package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.Configuration
import bazel.bazel.events.WorkspaceConfig
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class WorkspaceConfigHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is WorkspaceConfig) {
                val event = ctx.event.payload.content
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    ctx.onNext(ctx.messageFactory.createMessage(ctx.buildMessage().append(listOf("localExecRoot", event.localExecRoot).joinToStringEscaped(" = ").apply(Color.Items)).toString()))
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)
}