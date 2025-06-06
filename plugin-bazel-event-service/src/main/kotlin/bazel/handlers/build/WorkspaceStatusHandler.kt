package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class WorkspaceStatusHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasWorkspaceStatus()) {
            return false
        }

        val status = ctx.event.workspaceStatus
        if (ctx.verbosity.atLeast(Verbosity.Verbose) && status.itemCount > 0) {
            for (item in status.itemList) {
                ctx.writer.message(listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items))
            }
        }

        return true
    }
}
