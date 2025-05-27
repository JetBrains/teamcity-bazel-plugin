package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage

class ConvenienceSymlinkHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
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
        ctx.onNext(
            ctx.messageFactory.createMessage(
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
