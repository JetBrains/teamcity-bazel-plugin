package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.buildMessage

class StructuredCommandLineHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasStructuredCommandLine()) {
            return false
        }
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            val label =
                ctx.event
                    .structuredCommandLine
                    .commandLineLabel
                    .takeIf { it.isNotEmpty() } ?: "tool"

            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("Run ")
                        .append(label)
                        .toString(),
                ),
            )
        }

        return true
    }
}
