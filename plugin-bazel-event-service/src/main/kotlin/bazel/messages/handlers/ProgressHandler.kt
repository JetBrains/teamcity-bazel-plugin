package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.BuildStarted
import bazel.bazel.events.Progress
import bazel.messages.ServiceMessageContext

class ProgressHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.High

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is Progress) {
            val event = ctx.event.payload.content
            if(ctx.verbosity.atLeast(Verbosity.Minimal)) {
                if (!event.stdout.isEmpty()) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append(event.stdout.clean())
                                    .toString()
                    ))
                }

                if (!event.stderr.isEmpty()) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append(event.stderr.clean())
                                    .toString()))
                }
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}