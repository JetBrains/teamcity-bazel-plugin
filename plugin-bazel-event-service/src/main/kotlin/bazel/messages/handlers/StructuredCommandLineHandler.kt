package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext

class StructuredCommandLineHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasStructuredCommandLine()) {
            return false
        }
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            val label =
                ctx.bazelEvent
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
