package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class UnknownEventHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
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
