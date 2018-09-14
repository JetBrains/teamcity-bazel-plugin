package bazel.bazel.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.AbortReason
import bazel.bazel.events.Aborted
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class AbortedHandler(
        private val _abortReasonConverter: Converter<BuildEventStreamProtos.Aborted.AbortReason, AbortReason>)
    : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasAborted()) {
                val content = ctx.event.aborted
                Aborted(
                        ctx.id,
                        ctx.children,
                        content.description,
                        _abortReasonConverter.convert(content.reason))
            } else ctx.handlerIterator.next().handle(ctx)
}