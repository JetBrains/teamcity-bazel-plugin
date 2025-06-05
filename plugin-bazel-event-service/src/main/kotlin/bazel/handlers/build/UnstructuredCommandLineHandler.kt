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
import bazel.messages.joinToStringEscaped

class UnstructuredCommandLineHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasUnstructuredCommandLine()) {
            return notHandled()
        }

        return handled(
            sequence {
                val commandLine = ctx.event.unstructuredCommandLine
                val cmd = commandLine.argsList.joinToStringEscaped()
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append("Run ")
                                append(cmd.apply(Color.Details))
                            },
                        ),
                    )
                }
            },
        )
    }
}
