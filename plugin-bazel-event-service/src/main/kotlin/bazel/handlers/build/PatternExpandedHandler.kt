package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped

class PatternExpandedHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasExpanded()) {
            return false
        }

        val patterns = ctx.event.id.pattern.patternList
        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            val patterns = patterns.joinToStringEscaped(", ")
            ctx.onNext(
                MessageFactory.createMessage(
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
