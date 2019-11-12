package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.NamedSetOfFiles
import bazel.messages.ServiceMessageContext

class NamedSetOfFilesHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is NamedSetOfFiles) {
                val event = ctx.event.payload.content
                if (ctx.verbosity.atLeast(Verbosity.Detailed) && event.files.isNotEmpty()) {
                    for (file in event.files) {
                        ctx.onNext(ctx.messageFactory.createMessage(
                                ctx.buildMessage()
                                        .append(file.name)
                                        .toString()))
                    }
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)
}