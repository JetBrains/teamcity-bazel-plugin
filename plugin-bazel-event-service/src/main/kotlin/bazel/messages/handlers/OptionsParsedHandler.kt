package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.*
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class OptionsParsedHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is OptionsParsed) {
            val event = ctx.event.payload.content
            if (ctx.verbosity.atLeast(Verbosity.Detailed)) {

                if (event.startupOptions.isNotEmpty()) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append("startup options: ")
                                    .append(event.startupOptions.joinToStringEscaped().apply(Color.Details))
                                    .toString()))
                }

                if (event.explicitStartupOptions.isNotEmpty()) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append("explicit startup options: ")
                                    .append(event.explicitStartupOptions.joinToStringEscaped().apply(Color.Details))
                                    .toString()))
                }

                if (event.cmdLines.isNotEmpty()) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append("command line:")
                                    .append(event.cmdLines.joinToString().apply(Color.Details))
                                    .toString()))
                }

                if (event.explicitCmdLines.isNotEmpty()) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append("explicit command line: ")
                                    .append(event.explicitCmdLines.joinToString().apply(Color.Details))
                                    .toString()))
                }
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}