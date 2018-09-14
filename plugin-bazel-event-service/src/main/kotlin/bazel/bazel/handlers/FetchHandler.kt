package bazel.bazel.handlers

import bazel.HandlerPriority
import bazel.bazel.events.Fetch
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class FetchHandler: BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasFetch()) {
                val content = ctx.event.fetch
                var url: String = ""
                if (ctx.event.hasId() && ctx.event.id.hasFetch()) {
                    url = ctx.event.id.fetch.url
                }

                Fetch(
                        ctx.id,
                        ctx.children,
                        url,
                        content.success)
            } else ctx.handlerIterator.next().handle(ctx)
}