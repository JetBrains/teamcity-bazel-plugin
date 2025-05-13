

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.UnstructuredCommandLine
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class UnstructuredCommandLineHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is UnstructuredCommandLine) {
            val commandLine = ctx.event.payload.content
            val cmd = commandLine.args.joinToStringEscaped()
            if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("Run ")
                            .append(cmd.apply(Color.Details))
                            .toString(),
                    ),
                )
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
}
