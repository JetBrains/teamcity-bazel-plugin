package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class BuildToolLogsHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        if (payload is BazelEvent && payload.event.hasBuildToolLogs()) {
            if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
                return true
            }

            val logs = payload.event.buildToolLogs.logList
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
        return ctx.handlerIterator.next().handle(ctx)
    }
}
