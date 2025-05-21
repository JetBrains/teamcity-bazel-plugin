package bazel.messages.handlers

import bazel.Verbosity
import bazel.atLeast
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class BuildMetadataHandler : BazelEventHandler {
    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasBuildMetadata()) {
            return false
        }

        val event = ctx.bazelEvent.buildMetadata
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
