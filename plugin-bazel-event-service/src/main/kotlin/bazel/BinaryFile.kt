package bazel

import bazel.handlers.BuildEventHandlerChain
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.nio.file.Path

class BinaryFile(
    private val _eventFile: Path,
    private val _verbosity: Verbosity,
    private val _binaryStream: BinaryFileEventStream,
    private val _buildEventHandlerChain: BuildEventHandlerChain,
) {
    fun read(): AutoCloseable =
        _binaryStream.create(_eventFile).start {
            when (it) {
                is BinaryFileEventStream.Result.Error -> onError(it.throwable)
                is BinaryFileEventStream.Result.Event -> onEvent(it)
            }
        }

    private fun onError(err: Throwable) {
        val error = MessageFactory.createErrorMessage("Error during binary file read", err.toString())
        printMessage(error)
    }

    private fun onEvent(event: BinaryFileEventStream.Result.Event) {
        val ctx =
            BuildEventHandlerContext(
                _verbosity,
                getMessagePrefix(_verbosity, event.sequenceNumber),
                event.event,
            )
        _buildEventHandlerChain.handle(ctx).messages.forEach(::printMessage)
    }

    private fun printMessage(message: ServiceMessage) {
        println(message.asString())
    }
}
