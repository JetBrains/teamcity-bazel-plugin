package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage

class FetchHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasFetch()) {
            return false
        }
        val event = ctx.event.fetch

        val url =
            if (ctx.event.hasId() && ctx.event.id.hasFetch()) {
                ctx.event.id.fetch.url
            } else {
                "unknown url"
            }

        if (event.success && ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.onNext(
                MessageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("Fetch \"${url}\"")
                        .toString(),
                ),
            )
        } else if (!event.success && ctx.verbosity.atLeast(Verbosity.Normal)) {
            ctx.onNext(
                MessageFactory.createWarningMessage(
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
