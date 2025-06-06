package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply

class FetchHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasFetch()) {
            return false
        }

        val event = ctx.event.fetch
        val url =
            if (ctx.event.hasId() && ctx.event.id.hasFetch()) {
                ctx.event.id.fetch.url
            } else {
                "unknown url"
            }

        if (event.success && ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.writer.message("Fetch \"${url}\"")
        }

        if (!event.success && ctx.verbosity.atLeast(Verbosity.Normal)) {
            ctx.writer.warning("Fetch \"${url}\" - unsuccessful".apply(Color.Error))
        }

        return true
    }
}
