package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped

class UnstructuredCommandLineHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasUnstructuredCommandLine()) {
            return false
        }
        val commandLine = ctx.event.unstructuredCommandLine
        val cmd = commandLine.argsList.joinToStringEscaped()
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.emitMessage(
                MessageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("Run ")
                        .append(cmd.apply(Color.Details))
                        .toString(),
                ),
            )
        }

        return true
    }
}
