

package bazel.bazel.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.ConvenienceSymlink
import bazel.bazel.events.ConvenienceSymlinks
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class ConvenienceSymlinksIdentifiedHandler(
    private val _symlinkConverter: Converter<BuildEventStreamProtos.ConvenienceSymlink, ConvenienceSymlink>
) : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasConvenienceSymlinksIdentified()) {
                val content = ctx.event.convenienceSymlinksIdentified

                val symlinks = mutableListOf<ConvenienceSymlink>()
                for (i in 0 until content.convenienceSymlinksCount) {
                    symlinks.add(_symlinkConverter.convert(content.getConvenienceSymlinks(i)))
                }

                ConvenienceSymlinks(
                        ctx.id,
                        ctx.children,
                        symlinks)
            } else ctx.handlerIterator.next().handle(ctx)
}