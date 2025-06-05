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
import kotlin.collections.iterator

class ConfigurationHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasConfiguration()) {
            return notHandled()
        }

        return handled(
            sequence {
                val event = ctx.event.configuration
                if (!ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    return@sequence
                }

                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append(
                                listOf("platformName", event.platformName)
                                    .joinToStringEscaped(" = ")
                                    .apply(Color.Items),
                            )
                        },
                    ),
                )
                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append(
                                listOf("mnemonic", event.mnemonic).joinToStringEscaped(" = ").apply(Color.Items),
                            )
                        },
                    ),
                )
                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append(listOf("cpu", event.cpu).joinToStringEscaped(" = ").apply(Color.Items))
                        },
                    ),
                )

                if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    return@sequence
                }
                for (item in event.makeVariableMap) {
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append(
                                    listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items),
                                )
                            },
                        ),
                    )
                }
            },
        )
    }
}
