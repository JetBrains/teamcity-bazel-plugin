package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.file.FileConverter
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class BuildToolLogsHandler : BuildEventHandler {
    val fileConverter = FileConverter()

    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildToolLogs()) {
            return false
        }

        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            ctx.event.buildToolLogs.logList
                .filter { !it.name.isEmpty() }
                .forEach {
                    val file = fileConverter.convert(it)
                    ctx.writer.message("$file".apply(Color.Items))
                }
        }

        return true
    }
}
