package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext

class NamedSetOfFilesHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasNamedSetOfFiles()) {
            return false
        }

        val namedSet = ctx.event.namedSetOfFiles
        if (ctx.verbosity.atLeast(Verbosity.Detailed) && namedSet.filesCount > 0) {
            for (file in namedSet.filesList) {
                ctx.writer.message(file.name)
            }
        }

        return true
    }
}
