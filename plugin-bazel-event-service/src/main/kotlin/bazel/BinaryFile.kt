package bazel

import bazel.messages.*
import bazel.messages.handlers.RootBazelEventHandler
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.nio.file.Path

class BinaryFile(
    private val _eventFile: Path,
    private val _verbosity: Verbosity,
    private val _messageFactory: MessageFactory,
    private val _hierarchy: Hierarchy,
    private val _binaryStream: BinaryFileStream,
    private val _rootBazelEventHandler: RootBazelEventHandler,
) {
    fun read(): AutoCloseable =
        _binaryStream.create(_eventFile).start {
            when (it) {
                is BinaryFileStream.Result.Error -> onError(it.throwable)
                is BinaryFileStream.Result.Event -> onEvent(it)
            }
        }

    private fun onError(err: Throwable) {
        val error = _messageFactory.createErrorMessage("Error during binary file read", err.toString())
        printMessage(error)
    }

    private fun onEvent(event: BinaryFileStream.Result.Event) {
        val ctx =
            BazelEventHandlerContext(
                _messageFactory,
                _hierarchy,
                _verbosity,
                event.sequenceNumber,
                bazelEvent = event.event,
            ) { serviceMessage ->
                printMessage(serviceMessage)
            }
        val processed = _rootBazelEventHandler.handle(ctx)
        if (processed) {
            if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                printMessage(_messageFactory.createTraceMessage(ctx.bazelEvent.toString()))
            }
        }
    }

    private fun printMessage(message: ServiceMessage) {
        println(message.asString())
    }
}
