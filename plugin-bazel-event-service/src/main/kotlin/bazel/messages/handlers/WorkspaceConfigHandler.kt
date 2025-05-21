package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class WorkspaceConfigHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasWorkspaceInfo()) {
            return false
        }

        val info = ctx.bazelEvent.workspaceInfo
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
