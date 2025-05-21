package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.messages.ServiceMessageContext

class BuildEnqueuedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext): Boolean {
        if (!ctx.event.rawEvent.hasBuildEnqueued()) {
            return false
        }
        ctx.onNext(ctx.messageFactory.createMessage("Build enqueued"))
        return true
    }
}
