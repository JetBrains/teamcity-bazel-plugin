package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage

class UnknownEventHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.onNext(
                ctx.messageFactory.createWarningMessage(
                    ctx
                        .buildMessage()
                        .append("Unknown event: ${ctx.event}".apply(Color.Warning))
                        .toString(),
                ),
            )
        }

        return false
    }
}
