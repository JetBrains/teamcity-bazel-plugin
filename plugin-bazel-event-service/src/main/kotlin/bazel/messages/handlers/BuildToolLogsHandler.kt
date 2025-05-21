package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class BuildToolLogsHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasBuildToolLogs()) {
            return false
        }
        if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
            return true
        }

        val logs = ctx.bazelEvent.buildToolLogs.logList
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
