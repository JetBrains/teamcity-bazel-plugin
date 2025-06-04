package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage

class ConvenienceSymlinkHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasConvenienceSymlinksIdentified()) {
            return false
        }
        if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
            return true
        }
        val symlinks = ctx.event.convenienceSymlinksIdentified.convenienceSymlinksList
        val summary =
            symlinks.joinToString(", ") { symlink ->
                "${symlink.path} → ${symlink.target}"
            }
        ctx.emitMessage(
            MessageFactory.createMessage(
                ctx
                    .buildMessage()
                    .append("Convenience symlinks identified ")
                    .append(summary.apply(Color.Details))
                    .toString(),
            ),
        )

        return true
    }
}
