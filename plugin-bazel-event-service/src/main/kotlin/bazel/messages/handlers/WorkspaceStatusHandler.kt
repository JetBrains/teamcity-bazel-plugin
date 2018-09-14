package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.*
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class WorkspaceStatusHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is WorkspaceStatus) {
            val event = ctx.event.payload.content
            if (ctx.verbosity.atLeast(Verbosity.Detailed) && event.items.isNotEmpty()) {
                //ctx.onNext(ctx.messageFactory.createBlockOpened("Workspace Status", ""))
                for (item in event.items) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append(listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items))
                                    .toString()))
                }

                //ctx.onNext(ctx.messageFactory.createBlockClosed("Workspace Status"))
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}