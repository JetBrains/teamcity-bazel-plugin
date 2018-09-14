package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.PatternExpanded

class PatternExpandedHandler: BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasExpanded()) {
                val patterns = mutableListOf<String>()
                if (ctx.event.hasId() && ctx.event.id.hasPattern()) {
                    for (i in 0 until ctx.event.id.pattern.patternCount) {
                        patterns.add(ctx.event.id.pattern.getPattern(i))
                    }
                }

                PatternExpanded(
                        ctx.id,
                        ctx.children,
                        patterns)
            } else ctx.handlerIterator.next().handle(ctx)
}