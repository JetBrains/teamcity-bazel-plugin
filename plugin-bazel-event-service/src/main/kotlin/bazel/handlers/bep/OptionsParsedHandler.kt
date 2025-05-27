package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped

class OptionsParsedHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasOptionsParsed()) {
            return false
        }
        if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
            return true
        }

        val options = ctx.event.optionsParsed
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
}
