package bazel.handlers.bes

import bazel.handlers.BesEventHandler
import bazel.handlers.BesEventHandlerContext

class BuildEnqueuedHandler : BesEventHandler {
    override fun handle(ctx: BesEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildEnqueued()) {
            return false
        }
        ctx.onNext(ctx.messageFactory.createMessage("Build enqueued"))
        return true
    }
}
