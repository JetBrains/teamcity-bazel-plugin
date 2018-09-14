package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.BuildStarted

class BuildStartedHandler: BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasStarted()) {
                val content = ctx.event.started
                BuildStarted(
                        ctx.id,
                        ctx.children,
                        content.buildToolVersion,
                        content.command,
                        content.workingDirectory,
                        content.workspaceDirectory)
            } else ctx.handlerIterator.next().handle(ctx)
}