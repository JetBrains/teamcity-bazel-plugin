package bazel.bazel.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.TargetConfigured
import bazel.bazel.events.TestSize
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class TargetConfiguredHandler(
        private val _testSizeConverter: Converter<BuildEventStreamProtos.TestSize, TestSize>)
    : BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasConfigured() && ctx.event.hasId() && ctx.event.id.hasTargetConfigured()) {
                val content = ctx.event.configured
                val tags = mutableListOf<String>()
                for (i in 0 until content.tagCount) {
                    tags.add(content.getTag(i))
                }

                TargetConfigured(
                        ctx.id,
                        ctx.children,
                        ctx.event.id.targetConfigured.label,
                        ctx.event.id.targetConfigured.aspect,
                        content.targetKind,
                        _testSizeConverter.convert(content.testSize),
                        tags)
            } else ctx.handlerIterator.next().handle(ctx)
}