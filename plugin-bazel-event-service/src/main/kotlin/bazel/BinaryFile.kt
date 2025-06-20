package bazel

import bazel.handlers.BuildEventHandlerChain
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.*
import java.nio.file.Path

class BinaryFile(
    private val _messageWriter: MessageWriter,
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
        _messageWriter.error("Error during binary file read", err.toString())
    }

    private fun onEvent(event: BinaryFileEventStream.Result.Event) {
        val messagePrefix = MessagePrefix.build(_verbosity, event.sequenceNumber)
        val ctx =
            BuildEventHandlerContext(
                _verbosity,
                event.event,
                MessageWriter(messagePrefix) { _messageWriter.write(it) },
            )
        _buildEventHandlerChain.handle(ctx)
    }
}
