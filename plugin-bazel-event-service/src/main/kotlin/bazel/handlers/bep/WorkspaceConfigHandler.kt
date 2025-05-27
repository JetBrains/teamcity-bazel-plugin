package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage

class WorkspaceConfigHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasWorkspaceInfo()) {
            return false
        }

        val info = ctx.event.workspaceInfo
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("localExecRoot = ${info.localExecRoot}".apply(Color.Items))
                        .toString(),
                ),
            )
        }

        return true
    }
}
