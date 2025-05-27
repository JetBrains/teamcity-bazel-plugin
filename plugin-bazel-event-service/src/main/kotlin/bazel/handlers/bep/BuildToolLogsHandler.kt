package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage

class BuildToolLogsHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildToolLogs()) {
            return false
        }
        if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
            return true
        }

        val logs = ctx.event.buildToolLogs.logList
        for (log in logs) {
            if (log.name.isEmpty()) continue

            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("$log".apply(Color.Items))
                        .toString(),
                ),
            )
        }

        return true
    }
}
