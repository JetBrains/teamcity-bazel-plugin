package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.StructuredCommandLine

class StructuredCommandLineHandler: BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasStructuredCommandLine()) {
                val content = ctx.event.structuredCommandLine
                StructuredCommandLine(
                        ctx.id,
                        ctx.children,
                        if(content.commandLineLabel.isNotEmpty()) content.commandLineLabel else "tool" )
            } else ctx.handlerIterator.next().handle(ctx)
}