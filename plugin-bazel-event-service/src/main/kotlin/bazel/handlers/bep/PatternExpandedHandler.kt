package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped

class PatternExpandedHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasExpanded()) {
            return false
        }

        val patterns = ctx.event.id.pattern.patternList
        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            val patterns = patterns.joinToStringEscaped(", ")
            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("Pattern expanded ")
                        .append(patterns.apply(Color.Details))
                        .toString(),
                ),
            )
        }

        return true
    }
}
