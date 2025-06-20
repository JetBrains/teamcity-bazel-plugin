package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class OptionsParsedHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasOptionsParsed()) {
            return false
        }

        if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
            return true
        }

        val options = ctx.event.optionsParsed
        if (options.startupOptionsList.isNotEmpty()) {
            ctx.writer.message("Options " + options.startupOptionsList.joinToStringEscaped().apply(Color.Details))
        }

        if (options.explicitStartupOptionsList.isNotEmpty()) {
            ctx.writer.message(
                "Explicit options " + options.explicitStartupOptionsList.joinToStringEscaped().apply(Color.Details),
            )
        }

        if (options.cmdLineList.isNotEmpty()) {
            ctx.writer.message("Command " + options.cmdLineList.joinToString().apply(Color.Details))
        }

        if (options.explicitCmdLineList.isNotEmpty()) {
            ctx.writer.message("Explicit command " + options.explicitCmdLineList.joinToString().apply(Color.Details))
        }

        return true
    }
}
