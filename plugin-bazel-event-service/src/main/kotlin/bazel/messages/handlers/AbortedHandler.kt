package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.bazel.events.Aborted
import bazel.bazel.events.BazelEvent
import bazel.messages.ServiceMessageContext

class AbortedHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is Aborted) {
            val event = ctx.event.payload.content
            ctx.onNext(ctx.messageFactory.createBuildProblem(
                    ctx.buildMessage(false)
                            .append("Aborted. ${event.reason.description}")
                            .append(if (event.description.isNotBlank()) ": ${event.description}" else ".")
                            .toString(),
                    ctx.event.projectId,
                    "Aborted:${event.reason}:${event.reason.description}"))

            true
        } else ctx.handlerIterator.next().handle(ctx)
}