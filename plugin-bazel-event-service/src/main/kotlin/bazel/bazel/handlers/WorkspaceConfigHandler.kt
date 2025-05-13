

package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.WorkspaceConfig

class WorkspaceConfigHandler : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
        if (ctx.event.hasWorkspaceInfo()) {
            val content = ctx.event.workspaceInfo
            WorkspaceConfig(
                ctx.id,
                ctx.children,
                content.localExecRoot,
            )
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
}
