package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.messages.joinToStringEscaped

class BuildMetadataHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildMetadata()) {
            return false
        }
        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            val event = ctx.event.buildMetadata
            for (item in event.metadataMap) {
                ctx.writer.message(listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items))
            }
        }

        return true
    }
}
