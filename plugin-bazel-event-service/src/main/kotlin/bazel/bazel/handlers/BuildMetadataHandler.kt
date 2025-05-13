

package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.BuildMetadata

class BuildMetadataHandler : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
        if (ctx.event.hasBuildMetadata()) {
            val content = ctx.event.buildMetadata
            BuildMetadata(
                ctx.id,
                ctx.children,
                content.metadataMap,
            )
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
}
