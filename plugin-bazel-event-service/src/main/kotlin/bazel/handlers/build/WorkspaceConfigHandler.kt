package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage

class WorkspaceConfigHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasWorkspaceInfo()) {
            return false
        }

        val info = ctx.event.workspaceInfo
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.onNext(
                MessageFactory.createMessage(
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
