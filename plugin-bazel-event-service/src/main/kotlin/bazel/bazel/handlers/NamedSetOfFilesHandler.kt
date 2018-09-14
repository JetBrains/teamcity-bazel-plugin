package bazel.bazel.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.File
import bazel.bazel.events.NamedSetOfFiles
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class NamedSetOfFilesHandler(
        private val _fileConverter: Converter<BuildEventStreamProtos.File, File>)
    : BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasNamedSetOfFiles()) {
                val content = ctx.event.namedSetOfFiles
                val files = mutableListOf<File>()
                for (i in 0 until content.fileSetsCount) {
                    files.add(_fileConverter.convert(content.getFiles(i)))
                }

                NamedSetOfFiles(
                        ctx.id,
                        ctx.children,
                        files)
            } else ctx.handlerIterator.next().handle(ctx)
}