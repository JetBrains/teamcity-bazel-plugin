package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped

class WorkspaceStatusHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasWorkspaceStatus()) {
            return false
        }

        val status = ctx.event.workspaceStatus
        if (ctx.verbosity.atLeast(Verbosity.Verbose) && status.itemCount > 0) {
            for (item in status.itemList) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append(listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items))
                            .toString(),
                    ),
                )
            }
        }

        return true
    }
}
