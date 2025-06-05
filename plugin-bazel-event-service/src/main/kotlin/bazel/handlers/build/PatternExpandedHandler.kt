package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createMessage
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class PatternExpandedHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasExpanded()) {
            return notHandled()
        }

        return handled(
            sequence {
                if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    return@sequence
                }

                val patterns =
                    ctx.event.id.pattern.patternList
                        .joinToStringEscaped(", ")
                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append("Pattern expanded ")
                            append(patterns.apply(Color.Details))
                        },
                    ),
                )
            },
        )
    }
}
