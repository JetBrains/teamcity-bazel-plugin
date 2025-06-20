package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class ConfigurationHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasConfiguration()) {
            return false
        }

        val event = ctx.event.configuration
        if (!ctx.verbosity.atLeast(Verbosity.Detailed)) {
            return true
        }

        ctx.writer.run {
            message(
                listOf("platformName", event.platformName)
                    .joinToStringEscaped(" = ")
                    .apply(Color.Items),
            )
            message(
                listOf("mnemonic", event.mnemonic)
                    .joinToStringEscaped(" = ")
                    .apply(Color.Items),
            )
            message(
                listOf("cpu", event.cpu)
                    .joinToStringEscaped(" = ")
                    .apply(Color.Items),
            )
        }

        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            for (item in event.makeVariableMap) {
                ctx.writer.message(
                    listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items),
                )
            }
        }

        return true
    }
}
