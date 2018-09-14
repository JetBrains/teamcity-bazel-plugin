package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.*
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class NamedSetOfFilesHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is NamedSetOfFiles) {
            val event = ctx.event.payload.content
            if (ctx.verbosity.atLeast(Verbosity.Normal) && event.files.isNotEmpty()) {
                ctx.onNext(ctx.messageFactory.createBlockOpened("Named set of files", ""))

                for (file in event.files) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append(file.name.apply(Color.Items))
                                    .toString()))
                }

                ctx.onNext(ctx.messageFactory.createBlockClosed("Named set of files"))
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}