package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class PatternExpandedHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasExpanded()) {
            return false
        }

        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            val patterns =
                ctx.event.id.pattern
                    .patternList
                    .joinToStringEscaped(", ")
            ctx.writer.message("Pattern expanded " + patterns.apply(Color.Details))
        }

        return true
    }
}
