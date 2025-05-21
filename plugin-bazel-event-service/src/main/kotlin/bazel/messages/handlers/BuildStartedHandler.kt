package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.Id
import bazel.messages.BazelEventHandlerContext

class BuildStartedHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasStarted()) {
            return false
        }

        val event = ctx.bazelEvent.started
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
            ctx.hierarchy.createNode(Id(ctx.bazelEvent.id), ctx.bazelEvent.childrenList.map { Id(it) }, description) {
                it.onNext(it.messageFactory.createBlockClosed(description))
            }
        }

        return true
    }
}
