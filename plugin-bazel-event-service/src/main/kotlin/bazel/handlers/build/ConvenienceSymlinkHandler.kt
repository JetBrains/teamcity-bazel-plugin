package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class ConvenienceSymlinkHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasConvenienceSymlinksIdentified()) {
            return false
        }
        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            val symlinks = ctx.event.convenienceSymlinksIdentified.convenienceSymlinksList
            val summary =
                symlinks.joinToString(", ") { symlink ->
                    "${symlink.path} → ${symlink.target}"
                }
            ctx.writer.message("Convenience symlinks identified " + summary.apply(Color.Details))
        }
        return true
    }
}
