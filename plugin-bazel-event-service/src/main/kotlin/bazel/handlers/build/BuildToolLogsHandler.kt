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

class BuildToolLogsHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasBuildToolLogs()) {
            return notHandled()
        }

        return handled(
            sequence {
                if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    return@sequence
                }

                val logs = ctx.event.buildToolLogs.logList
                for (log in logs) {
                    if (log.name.isEmpty()) continue

                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append("$log".apply(Color.Items))
                            },
                        ),
                    )
                }
            },
        )
    }
}
