

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.BuildToolLogs
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class BuildToolLogsHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is BuildToolLogs) {
            val event = ctx.event.payload.content
            if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                for (log in event.logs) {
                    if (log.name.isNullOrEmpty()) {
                        continue
                    }

                    ctx.onNext(
                        ctx.messageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append("$log".apply(Color.Items))
                                .toString(),
                        ),
                    )
                }
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
}
