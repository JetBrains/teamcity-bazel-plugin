package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext

class StructuredCommandLineHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasStructuredCommandLine()) {
            return false
        }

        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            val label =
                ctx.event
                    .structuredCommandLine
                    .commandLineLabel
                    .takeIf { it.isNotEmpty() } ?: "tool"

            ctx.writer.message("Run $label")
        }

        return true
    }
}
