package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createMessage
import bazel.messages.apply

class ConvenienceSymlinkHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasConvenienceSymlinksIdentified()) {
            return notHandled()
        }
        return handled(
            sequence {
                if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    return@sequence
                }
                val symlinks = ctx.event.convenienceSymlinksIdentified.convenienceSymlinksList
                val summary =
                    symlinks.joinToString(", ") { symlink ->
                        "${symlink.path} → ${symlink.target}"
                    }
                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append("Convenience symlinks identified ")
                            append(summary.apply(Color.Details))
                        },
                    ),
                )
            },
        )
    }
}
