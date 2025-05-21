package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class UnstructuredCommandLineHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasUnstructuredCommandLine()) {
            return false
        }
        val commandLine = ctx.bazelEvent.unstructuredCommandLine
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
