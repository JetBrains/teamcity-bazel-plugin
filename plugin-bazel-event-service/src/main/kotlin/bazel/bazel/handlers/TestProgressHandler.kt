

package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.TestProgress

class TestProgressHandler : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasTestProgress()) {
                val content = ctx.event.testProgress
                TestProgress(
                        ctx.id,
                        ctx.children,
                        content.uri)
            } else ctx.handlerIterator.next().handle(ctx)
}