package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createMessage
import bazel.messages.apply

class WorkspaceConfigHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasWorkspaceInfo()) {
            return notHandled()
        }

        return handled(
            sequence {
                val info = ctx.event.workspaceInfo
                if (!ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    return@sequence
                }
                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append("localExecRoot = ${info.localExecRoot}".apply(Color.Items))
                        },
                    ),
                )
            },
        )
    }
}
