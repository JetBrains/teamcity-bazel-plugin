package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.events.BuildEnqueued
import bazel.messages.ServiceMessageContext

class BuildEnqueuedHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BuildEnqueued) {
            ctx.onNext(ctx.messageFactory.createBuildStatus("Build enqueued"))
            true
        } else ctx.handlerIterator.next().handle(ctx)
}