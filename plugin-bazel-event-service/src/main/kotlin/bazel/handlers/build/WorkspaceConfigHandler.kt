package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class WorkspaceConfigHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasWorkspaceInfo()) {
            return false
        }

        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            val info = ctx.event.workspaceInfo
            ctx.writer.message("localExecRoot = ${info.localExecRoot}".apply(Color.Items))
        }

        return true
    }
}
