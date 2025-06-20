package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class UnknownEventHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.writer.warning("Unknown event: ${ctx.event}".apply(Color.Warning))
        }
        return true
    }
}
