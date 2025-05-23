package bazel.messages.handlers

import bazel.messages.BuildEventHandlerContext

class BuildEnqueuedHandler : EventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildEnqueued()) {
            return false
        }
        ctx.onNext(ctx.messageFactory.createMessage("Build enqueued"))
        return true
    }
}
