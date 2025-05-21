package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class WorkspaceStatusHandler : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        if (payload is BazelEvent && payload.event.hasWorkspaceStatus()) {
            val status = payload.event.workspaceStatus

            if (ctx.verbosity.atLeast(Verbosity.Verbose) && status.itemCount > 0) {
                for (item in status.itemList) {
                    ctx.onNext(
                        ctx.messageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append(listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items))
                                .toString(),
                        ),
                    )
                }
            }

            return true
        }
        return ctx.handlerIterator.next().handle(ctx)
    }
}
