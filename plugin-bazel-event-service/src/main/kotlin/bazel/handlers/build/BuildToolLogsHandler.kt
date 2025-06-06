package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class BuildToolLogsHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildToolLogs()) {
            return false
        }

        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            ctx.event.buildToolLogs.logList
                .filter { !it.name.isEmpty() }
                .forEach {
                    ctx.writer.message("$it".apply(Color.Items))
                }
        }

        return true
    }
}
