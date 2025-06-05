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
import bazel.messages.MessageFactory.createWarningMessage
import bazel.messages.apply

class FetchHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasFetch()) {
            return notHandled()
        }

        return handled(
            sequence {
                val event = ctx.event.fetch

                val url =
                    if (ctx.event.hasId() && ctx.event.id.hasFetch()) {
                        ctx.event.id.fetch.url
                    } else {
                        "unknown url"
                    }

                if (event.success && ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append("Fetch \"${url}\"")
                            },
                        ),
                    )
                } else if (!event.success && ctx.verbosity.atLeast(Verbosity.Normal)) {
                    yield(
                        createWarningMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append("Fetch \"${url}\" - unsuccessful".apply(Color.Error))
                            },
                        ),
                    )
                }
            },
        )
    }
}
