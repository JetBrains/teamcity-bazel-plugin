package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.messages.Color
import bazel.messages.MessageFactory.createWarningMessage
import bazel.messages.apply

class UnknownEventHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult =
        handled(
            sequence {
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    yield(
                        createWarningMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append("Unknown event: ${ctx.event}".apply(Color.Warning))
                            },
                        ),
                    )
                }
            },
        )
}
