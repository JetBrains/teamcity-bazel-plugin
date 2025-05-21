package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class FetchHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasFetch()) {
            return false
        }
        val event = ctx.bazelEvent.fetch

        val url =
            if (ctx.bazelEvent.hasId() && ctx.bazelEvent.id.hasFetch()) {
                ctx.bazelEvent.id.fetch.url
            } else {
                "unknown url"
            }

        if (event.success && ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("Fetch \"${url}\"")
                        .toString(),
                ),
            )
        } else if (!event.success && ctx.verbosity.atLeast(Verbosity.Normal)) {
            ctx.onNext(
                ctx.messageFactory.createWarningMessage(
                    ctx
                        .buildMessage()
                        .append("Fetch \"${url}\" - unsuccessful".apply(Color.Error))
                        .toString(),
                ),
            )
        }

        return true
    }
}
