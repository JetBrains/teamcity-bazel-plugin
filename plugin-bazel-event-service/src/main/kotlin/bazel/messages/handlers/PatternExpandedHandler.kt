package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class PatternExpandedHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasExpanded()) {
            return false
        }

        val patterns = ctx.bazelEvent.id.pattern.patternList
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
