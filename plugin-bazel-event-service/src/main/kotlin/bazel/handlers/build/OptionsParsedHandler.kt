package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createMessage
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class OptionsParsedHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasOptionsParsed()) {
            return notHandled()
        }

        return handled(
            sequence {
                if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    return@sequence
                }

                val options = ctx.event.optionsParsed
                if (options.startupOptionsList.isNotEmpty()) {
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append("Options ")
                                append(options.startupOptionsList.joinToStringEscaped().apply(Color.Details))
                            },
                        ),
                    )
                }

                if (options.explicitStartupOptionsList.isNotEmpty()) {
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append("Explicit options ")
                                append(options.explicitStartupOptionsList.joinToStringEscaped().apply(Color.Details))
                            },
                        ),
                    )
                }

                if (options.cmdLineList.isNotEmpty()) {
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append("Command ")
                                append(options.cmdLineList.joinToString().apply(Color.Details))
                            },
                        ),
                    )
                }

                if (options.explicitCmdLineList.isNotEmpty()) {
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append("Explicit command ")
                                append(options.explicitCmdLineList.joinToString().apply(Color.Details))
                            },
                        ),
                    )
                }
            },
        )
    }
}
