package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.Progress

class ProgressHandler: BazelHandler {
    override val priority = HandlerPriority.High

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasProgress()) {
                val content = ctx.event.progress
                Progress(
                        ctx.id,
                        ctx.children,
                        content.stdout,
                        content.stderr)
            } else ctx.handlerIterator.next().handle(ctx)
}