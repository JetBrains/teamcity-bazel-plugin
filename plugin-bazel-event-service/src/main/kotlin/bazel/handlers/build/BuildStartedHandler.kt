package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.buildMessage

class BuildStartedHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasStarted()) {
            return false
        }

        val event = ctx.event.started
        val description = event.command
        val details =
            ctx
                .buildMessage(false)
                .append(description, Verbosity.Normal)
                .append(" v${event.buildToolVersion}", Verbosity.Verbose)
                .append(", directory: \"${event.workingDirectory}\"", Verbosity.Verbose)
                .append(", workspace: \"${event.workspaceDirectory}\"", Verbosity.Verbose)
                .toString()

        if (ctx.verbosity.atLeast(Verbosity.Normal)) {
            ctx.onNext(ctx.messageFactory.createBlockOpened(description, details))
            ctx.hierarchy.createNode(ctx.event.id, ctx.event.childrenList, description) {
                ctx.onNext(ctx.messageFactory.createBlockClosed(description))
            }
        }

        return true
    }
}
