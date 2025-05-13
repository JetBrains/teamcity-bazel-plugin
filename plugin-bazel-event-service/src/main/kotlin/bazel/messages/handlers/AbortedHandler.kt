

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.bazel.events.Aborted
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class AbortedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is Aborted) {
            val event = ctx.event.payload.content
            ctx.hierarchy.tryAbortNode(ctx, event.id)?.let {
                if (it.description.isNotEmpty()) {
                    ctx.onNext(
                        ctx.messageFactory
                            .createMessage(
                                ctx
                                    .buildMessage(false)
                                    .append(it.description)
                                    .append(" aborted.".apply(Color.Error))
                                    .append(" ${event.reason.description}")
                                    .append(if (event.description.isNotBlank()) ": ${event.description}" else ".")
                                    .toString(),
                            ),
                    )
                }
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
}
