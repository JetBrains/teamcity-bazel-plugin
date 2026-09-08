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
    private val _reportTargetLogToBuildLog: Boolean,
    private val _buildEventHandlerChain: BuildEventHandlerChain,
    private val _pendingDiagnostics: PendingInvocationDiagnostics,
) {
    fun read(): AutoCloseable {
        val stream =
            _binaryStream.create(_eventFile).start {
                when (it) {
                    is BinaryFileEventStream.Result.Error -> onError(it.throwable)
                    is BinaryFileEventStream.Result.Event -> onEvent(it)
                    is BinaryFileEventStream.Result.StreamRestarted -> onStreamRestarted()
                }
            }

        return AutoCloseable {
            // Closing joins the reader thread, so the flush sees the whole stream. It runs even if
            // closing fails, or a failure there would take the buffered diagnostics down with it.
            try {
                stream.close()
            } finally {
                _pendingDiagnostics.flush(_messageWriter)
            }
        }
    }

    private fun onError(err: Throwable) {
        _messageWriter.error("Error during binary file read", err.toString())
    }

    /**
     * Proof of a restart that cannot be missed, unlike the superseded attempt's `BuildFinished`
     * event, which the rewrite can overwrite before the reader gets to it.
     */
    private fun onStreamRestarted() = _pendingDiagnostics.discardSuperseded(_messageWriter)

    private fun onEvent(event: BinaryFileEventStream.Result.Event) {
        val messagePrefix = MessagePrefix.build(_verbosity, event.sequenceNumber)
        val ctx =
            BuildEventHandlerContext(
                _verbosity,
                event.event,
                MessageWriter(messagePrefix) { _messageWriter.write(it) },
                _reportTargetLogToBuildLog,
            )
        _buildEventHandlerChain.handle(ctx)
    }
}
