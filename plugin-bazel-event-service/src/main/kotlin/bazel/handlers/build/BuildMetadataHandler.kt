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

class BuildMetadataHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasBuildMetadata()) {
            return notHandled()
        }
        return handled(
            sequence {
                val event = ctx.event.buildMetadata
                if (!ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    return@sequence
                }

                for (item in event.metadataMap) {
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append(listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items))
                            },
                        ),
                    )
                }
            },
        )
    }
}
