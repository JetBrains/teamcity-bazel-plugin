package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class WorkspaceConfigHandler : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        if (payload is BazelEvent && payload.rawEvent.hasWorkspaceInfo()) {
            val info = ctx.event.payload.rawEvent.workspaceInfo

            if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("localExecRoot = ${info.localExecRoot}".apply(Color.Items))
                            .toString(),
                    ),
                )
            }

            return true
        }
        return ctx.handlerIterator.next().handle(ctx)
    }
}
