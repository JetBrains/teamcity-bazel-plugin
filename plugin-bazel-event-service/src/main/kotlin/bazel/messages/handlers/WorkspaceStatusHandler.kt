package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class WorkspaceStatusHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasWorkspaceStatus()) {
            return false
        }

        val status = ctx.bazelEvent.workspaceStatus
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
