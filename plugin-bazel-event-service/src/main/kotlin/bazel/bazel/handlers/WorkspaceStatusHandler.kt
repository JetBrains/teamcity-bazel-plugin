package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.WorkspaceStatus

class WorkspaceStatusHandler: BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasWorkspaceStatus()) {
                val content = ctx.event.workspaceStatus
                val items = mutableMapOf<String, String>()
                for (i in 0 until content.itemCount) {
                    val item = content.getItem(i)
                    items[item.key] = item.value
                }
                WorkspaceStatus(
                        ctx.id,
                        ctx.children,
                        items)
            } else ctx.handlerIterator.next().handle(ctx)
}