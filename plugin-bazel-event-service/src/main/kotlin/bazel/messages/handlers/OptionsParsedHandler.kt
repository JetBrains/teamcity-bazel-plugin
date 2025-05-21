package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class OptionsParsedHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasOptionsParsed()) {
            return false
        }
        if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
            return true
        }

        val options = ctx.bazelEvent.optionsParsed
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
