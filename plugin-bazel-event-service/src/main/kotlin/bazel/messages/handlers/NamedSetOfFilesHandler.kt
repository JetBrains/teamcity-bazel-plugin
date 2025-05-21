package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext

class NamedSetOfFilesHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasNamedSetOfFiles()) {
            return false
        }

        val namedSet = ctx.bazelEvent.namedSetOfFiles
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
