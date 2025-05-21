package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.ServiceMessageContext

class NamedSetOfFilesHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        if (payload is BazelEvent && payload.event.hasNamedSetOfFiles()) {
            val namedSet = payload.event.namedSetOfFiles
            if (!ctx.verbosity.atLeast(Verbosity.Detailed) || namedSet.filesCount == 0) {
                return true
            }

            for (file in namedSet.filesList) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append(file.name)
                            .toString(),
                    ),
                )
            }

            return true
        }
        return ctx.handlerIterator.next().handle(ctx)
    }
}
