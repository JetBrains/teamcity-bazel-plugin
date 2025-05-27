package bazel

import bazel.handlers.BepEventHandlerChain
import bazel.handlers.BepEventHandlerContext
import bazel.messages.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.nio.file.Path

class BinaryFile(
    private val _eventFile: Path,
    private val _verbosity: Verbosity,
    private val _messageFactory: MessageFactory,
    private val _hierarchy: Hierarchy,
    private val _binaryStream: BinaryFileEventStream,
    private val _bepEventHandlerChain: BepEventHandlerChain,
) {
    fun read(): AutoCloseable =
        _binaryStream.create(_eventFile).start {
            when (it) {
                is BinaryFileEventStream.Result.Error -> onError(it.throwable)
                is BinaryFileEventStream.Result.Event -> onEvent(it)
            }
        }

    private fun onError(err: Throwable) {
        val error = _messageFactory.createErrorMessage("Error during binary file read", err.toString())
        printMessage(error)
    }

    private fun onEvent(event: BinaryFileEventStream.Result.Event) {
        val ctx =
            BepEventHandlerContext(
                _verbosity,
                event.sequenceNumber,
                messageFactory = _messageFactory,
                hierarchy = _hierarchy,
                event = event.event,
            ) { serviceMessage ->
                printMessage(serviceMessage)
            }
        val processed = _bepEventHandlerChain.handle(ctx)
        if (processed) {
            if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                printMessage(_messageFactory.createTraceMessage(ctx.event.toString()))
            }
        }
    }

    private fun printMessage(message: ServiceMessage) {
        println(message.asString())
    }
}
