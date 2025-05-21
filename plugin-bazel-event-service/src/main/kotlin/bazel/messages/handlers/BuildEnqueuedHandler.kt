package bazel.messages.handlers

import bazel.messages.ServiceMessageContext

class BuildEnqueuedHandler : EventHandler {
    override fun handle(ctx: ServiceMessageContext): Boolean {
        if (!ctx.event.rawEvent.hasBuildEnqueued()) {
            return false
        }
        ctx.onNext(ctx.messageFactory.createMessage("Build enqueued"))
        return true
    }
}
