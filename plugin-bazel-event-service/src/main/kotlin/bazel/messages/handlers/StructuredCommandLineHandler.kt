package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.ServiceMessageContext

class StructuredCommandLineHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        if (payload is BazelEvent && payload.rawEvent.hasStructuredCommandLine()) {
            if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                val label =
                    ctx.event.payload.rawEvent.structuredCommandLine.commandLineLabel
                        .takeIf { it.isNotEmpty() } ?: "tool"

                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("Run ")
                            .append(label)
                            .toString(),
                    ),
                )
            }

            return true
        }
        return ctx.handlerIterator.next().handle(ctx)
    }
}
