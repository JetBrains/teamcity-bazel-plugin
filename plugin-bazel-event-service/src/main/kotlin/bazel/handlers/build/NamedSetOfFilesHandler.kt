package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.MessageFactory
import bazel.messages.buildMessage

class NamedSetOfFilesHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasNamedSetOfFiles()) {
            return false
        }

        val namedSet = ctx.event.namedSetOfFiles
        if (!ctx.verbosity.atLeast(Verbosity.Detailed) || namedSet.filesCount == 0) {
            return true
        }

        for (file in namedSet.filesList) {
            ctx.emitMessage(
                MessageFactory.createMessage(
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
