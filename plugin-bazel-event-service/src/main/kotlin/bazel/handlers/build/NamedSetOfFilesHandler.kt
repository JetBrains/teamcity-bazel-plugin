package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.MessageFactory.createMessage

class NamedSetOfFilesHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasNamedSetOfFiles()) {
            return notHandled()
        }

        return handled(
            sequence {
                val namedSet = ctx.event.namedSetOfFiles
                if (!ctx.verbosity.atLeast(Verbosity.Detailed) || namedSet.filesCount == 0) {
                    return@sequence
                }

                for (file in namedSet.filesList) {
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append(file.name)
                            },
                        ),
                    )
                }
            },
        )
    }
}
