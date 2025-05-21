package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class ConvenienceSymlinkHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        if (payload is BazelEvent && payload.event.hasConvenienceSymlinksIdentified()) {
            if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
                return true
            }
            val symlinks = payload.event.convenienceSymlinksIdentified.convenienceSymlinksList
            val summary =
                symlinks.joinToString(", ") { symlink ->
                    "${symlink.path} → ${symlink.target}"
                }
            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("Convenience symlinks identified ")
                        .append(summary.apply(Color.Details))
                        .toString(),
                ),
            )

            return true
        }
        return ctx.handlerIterator.next().handle(ctx)
    }
}
