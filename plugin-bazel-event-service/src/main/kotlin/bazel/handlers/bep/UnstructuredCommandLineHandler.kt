package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped

class UnstructuredCommandLineHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasUnstructuredCommandLine()) {
            return false
        }
        val commandLine = ctx.event.unstructuredCommandLine
        val cmd = commandLine.argsList.joinToStringEscaped()
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.onNext(
                ctx.messageFactory.createMessage(
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
