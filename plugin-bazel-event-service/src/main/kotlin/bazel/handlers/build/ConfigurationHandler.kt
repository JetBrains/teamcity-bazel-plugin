package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped
import kotlin.collections.iterator

class ConfigurationHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasConfiguration()) {
            return false
        }

        val event = ctx.event.configuration
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append(
                            listOf("platformName", event.platformName)
                                .joinToStringEscaped(" = ")
                                .apply(Color.Items),
                        ).toString(),
                ),
            )
            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append(
                            listOf("mnemonic", event.mnemonic).joinToStringEscaped(" = ").apply(Color.Items),
                        ).toString(),
                ),
            )
            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append(listOf("cpu", event.cpu).joinToStringEscaped(" = ").apply(Color.Items))
                        .toString(),
                ),
            )

            if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                for (item in event.makeVariableMap) {
                    ctx.onNext(
                        ctx.messageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append(
                                    listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items),
                                ).toString(),
                        ),
                    )
                }
            }
        }

        return true
    }
}
