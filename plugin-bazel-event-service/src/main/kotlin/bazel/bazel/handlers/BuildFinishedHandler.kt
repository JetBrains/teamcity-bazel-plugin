package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.BuildFinished

class BuildFinishedHandler: BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasFinished() && ctx.event.finished.hasExitCode()) {
                val content = ctx.event.finished
                BuildFinished(
                        ctx.id,
                        ctx.children,
                        content.exitCode.code,
                        content.exitCode.name)
            } else ctx.handlerIterator.next().handle(ctx)
}