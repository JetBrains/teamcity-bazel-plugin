package bazel.v1.handlers

import bazel.Converter
import bazel.HandlerPriority
import bazel.events.ConsoleOutput
import bazel.events.OrderedBuildEvent
import com.google.devtools.build.v1.ConsoleOutputStream

class ConsoleOutputHandler(
        private val _consoleOutputStreamConverter: Converter<ConsoleOutputStream, bazel.events.ConsoleOutputStream>)
    : EventHandler {
    override val priority: HandlerPriority = HandlerPriority.High

    override fun handle(ctx: HandlerContext): OrderedBuildEvent =
            if (ctx.event.hasConsoleOutput()) {
                ConsoleOutput(
                        ctx.streamId,
                        ctx.sequenceNumber,
                        ctx.eventTime,
                        _consoleOutputStreamConverter.convert(ctx.event.consoleOutput.type),
                        ctx.event.consoleOutput.textOutput)
            } else ctx.handlerIterator.next().handle(ctx)
}