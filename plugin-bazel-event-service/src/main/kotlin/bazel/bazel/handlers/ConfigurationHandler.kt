package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.Configuration

class ConfigurationHandler: BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasConfiguration()) {
                val content = ctx.event.configuration
                Configuration(
                        ctx.id,
                        ctx.children,
                        content.platformName,
                        content.mnemonic,
                        content.cpu,
                        content.makeVariableMap)
            } else ctx.handlerIterator.next().handle(ctx)
}