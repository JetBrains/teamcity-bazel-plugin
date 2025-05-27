package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.buildMessage

class NamedSetOfFilesHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasNamedSetOfFiles()) {
            return false
        }

        val namedSet = ctx.event.namedSetOfFiles
        if (!ctx.verbosity.atLeast(Verbosity.Detailed) || namedSet.filesCount == 0) {
            return true
        }

        for (file in namedSet.filesList) {
            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append(file.name)
                        .toString(),
                ),
            )
        }

        return true
    }
}
