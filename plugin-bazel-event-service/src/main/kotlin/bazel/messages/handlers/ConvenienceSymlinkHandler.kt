

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.ConvenienceSymlinks
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class ConvenienceSymlinkHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is ConvenienceSymlinks) {
            val event = ctx.event.payload.content
            if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                val symlinks = event.symlinks.joinToStringEscaped(", ")
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("Convenience symlinks identified ")
                            .append(symlinks.apply(Color.Details))
                            .toString(),
                    ),
                )
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
}
