package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.OptionsParsed

class OptionsParsedHandler: BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasOptionsParsed()) {
                val content = ctx.event.optionsParsed

                val cmdLines = mutableListOf<String>()
                for (i in 0 until content.cmdLineCount) {
                    cmdLines.add(content.getCmdLine(i))
                }

                val explicitCmdLines = mutableListOf<String>()
                for (i in 0 until content.explicitCmdLineCount) {
                    explicitCmdLines.add(content.getExplicitCmdLine(i))
                }

                val startupOptions = mutableListOf<String>()
                for (i in 0 until content.startupOptionsCount) {
                    startupOptions.add(content.getStartupOptions(i))
                }

                val explicitStartupOptions = mutableListOf<String>()
                for (i in 0 until content.explicitStartupOptionsCount) {
                    explicitStartupOptions.add(content.getExplicitStartupOptions(i))
                }

                OptionsParsed(
                        ctx.id,
                        ctx.children,
                        cmdLines,
                        explicitCmdLines,
                        startupOptions,
                        explicitStartupOptions)
            } else ctx.handlerIterator.next().handle(ctx)
}