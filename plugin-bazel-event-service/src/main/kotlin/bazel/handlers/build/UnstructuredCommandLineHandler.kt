package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class UnstructuredCommandLineHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasUnstructuredCommandLine()) {
            return false
        }

        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            val cmd =
                ctx.event.unstructuredCommandLine
                    .argsList
                    .joinToStringEscaped()
            ctx.writer.message("Run " + cmd.apply(Color.Details))
        }

        return true
    }
}
