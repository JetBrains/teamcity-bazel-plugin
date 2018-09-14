package bazel.bazel.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.bazel.events.ActionExecuted
import bazel.bazel.events.File
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos

class ActionExecutedHandler(
        private val _fileConverter: Converter<BuildEventStreamProtos.File, File>)
    : BazelHandler {
    override val priority = HandlerPriority.Medium

    override fun handle(ctx: HandlerContext) =
            if (ctx.event.hasAction()) {
                val content = ctx.event.action
                val cmdLines = mutableListOf<String>()
                for (i in 0 until content.commandLineCount) {
                    cmdLines.add(content.getCommandLine(i))
                }

                ActionExecuted(
                        ctx.id,
                        ctx.children,
                        content.type,
                        cmdLines,
                        content.success,
                        _fileConverter.convert(content.primaryOutput),
                        _fileConverter.convert(content.stdout),
                        _fileConverter.convert(content.stderr),
                        content.exitCode)
            } else ctx.handlerIterator.next().handle(ctx)
}