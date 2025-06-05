package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.MessageFactory

class StructuredCommandLineHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasStructuredCommandLine()) {
            return notHandled()
        }
        return handled(
            sequence {
                if (!ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    return@sequence
                }
                val label =
                    ctx.event.structuredCommandLine.commandLineLabel
                        .takeIf { it.isNotEmpty() } ?: "tool"

                yield(
                    MessageFactory.createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append("Run $label")
                        },
                    ),
                )
            },
        )
    }
}
