

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.ExecRequest
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class UnknownEventHandler : EventHandler {
    override val priority: HandlerPriority get() = HandlerPriority.Last

    override fun handle(ctx: ServiceMessageContext): Boolean {
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is ExecRequest) {
            // just skip
            return true
        }

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
