package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class ConvenienceSymlinkHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasConvenienceSymlinksIdentified()) {
            return false
        }
        if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
            return true
        }
        val symlinks = ctx.bazelEvent.convenienceSymlinksIdentified.convenienceSymlinksList
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
