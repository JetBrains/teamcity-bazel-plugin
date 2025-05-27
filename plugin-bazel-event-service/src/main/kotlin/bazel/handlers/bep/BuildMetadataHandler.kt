package bazel.handlers.bep

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BepEventHandler
import bazel.handlers.BepEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped
import kotlin.collections.iterator

class BuildMetadataHandler : BepEventHandler {
    override fun handle(ctx: BepEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildMetadata()) {
            return false
        }

        val event = ctx.event.buildMetadata
        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            for (item in event.metadataMap) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append(listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items))
                            .toString(),
                    ),
                )
            }
        }

        return true
    }
}
