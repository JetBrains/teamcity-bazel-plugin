package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.TargetComplete

class TargetCompleteHandler: BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasCompleted() && ctx.event.hasId() && ctx.event.id.hasTargetCompleted()) {
                val content = ctx.event.completed
                val tags = mutableListOf<String>()
                for (i in 0 until content.tagCount) {
                    tags.add(content.getTag(i))
                }

                TargetComplete(
                        ctx.id,
                        ctx.children,
                        ctx.event.id.targetCompleted.label,
                        content.success,
                        tags,
                        content.testTimeoutSeconds)
            } else ctx.handlerIterator.next().handle(ctx)
}