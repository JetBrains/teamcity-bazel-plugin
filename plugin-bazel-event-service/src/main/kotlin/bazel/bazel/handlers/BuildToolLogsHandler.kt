package bazel.bazel.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.BuildToolLogs
import bazel.bazel.events.File
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class BuildToolLogsHandler(
        private val _fileConverter: Converter<BuildEventStreamProtos.File, File>)
    : BazelHandler {
    override val priority = HandlerPriority.Low

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasBuildToolLogs()) {
                val content = ctx.event.buildToolLogs
                val logs = mutableListOf<File>()
                for (i in 0 until content.logCount) {
                    logs.add(_fileConverter.convert(content.getLog(i)))
                }

                BuildToolLogs(
                        ctx.id,
                        ctx.children,
                        logs)
            } else ctx.handlerIterator.next().handle(ctx)
}