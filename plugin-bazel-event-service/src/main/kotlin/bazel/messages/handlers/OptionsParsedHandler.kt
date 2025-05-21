package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class OptionsParsedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        if (payload is BazelEvent && payload.event.hasOptionsParsed()) {
            if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
                return true
            }

            val options = payload.event.optionsParsed
            if (options.startupOptionsList.isNotEmpty()) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("Options ")
                            .append(options.startupOptionsList.joinToStringEscaped().apply(Color.Details))
                            .toString(),
                    ),
                )
            }

            if (options.explicitStartupOptionsList.isNotEmpty()) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("Explicit options ")
                            .append(options.explicitStartupOptionsList.joinToStringEscaped().apply(Color.Details))
                            .toString(),
                    ),
                )
            }

            if (options.cmdLineList.isNotEmpty()) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("Command ")
                            .append(options.cmdLineList.joinToString().apply(Color.Details))
                            .toString(),
                    ),
                )
            }

            if (options.explicitCmdLineList.isNotEmpty()) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append("Explicit command ")
                            .append(options.explicitCmdLineList.joinToString().apply(Color.Details))
                            .toString(),
                    ),
                )
            }

            return true
        }
        return ctx.handlerIterator.next().handle(ctx)
    }
}
