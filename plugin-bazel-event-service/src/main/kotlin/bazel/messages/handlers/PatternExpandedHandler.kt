package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class PatternExpandedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        return if (payload is BazelEvent && payload.event.hasExpanded()) {
            val patterns = payload.event.id.pattern.patternList
            if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                val patterns = patterns.joinToStringEscaped(", ")
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("Pattern expanded ")
                            .append(patterns.apply(Color.Details))
                            .toString(),
                    ),
                )
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
    }
}
