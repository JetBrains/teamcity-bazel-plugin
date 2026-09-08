package bazel

import bazel.handlers.GrpcEventHandlerChain
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.MessagePrefix
import bazel.messages.MessageWriter
import bazel.messages.PendingInvocationDiagnostics
import java.util.Date

class BesGrpcServer(
    private val _messageWriter: MessageWriter,
    private val _grpcServer: GrpcServer,
    private val _verbosity: Verbosity,
    private val _reportTargetLogToBuildLog: Boolean,
    private val _buildEventHandler: GrpcEventHandlerChain,
    private val _pendingDiagnostics: PendingInvocationDiagnostics,
) {
    /**
     * Once an event has arrived, the stream itself has carried Bazel's stderr into the build log,
     * so the caller must not replay the stderr [BazelRunner] collected on top of it.
     */
    @Volatile
    var hasStarted = false
        private set

    fun start(): AutoCloseable {
        val server =
            _grpcServer
                .start(
                    BesGrpcServerEventStream(_messageWriter) {
                        when (it) {
                            is BesGrpcServerEventStream.Result.Event -> onEvent(it)
                            is BesGrpcServerEventStream.Result.Error -> onError(it.throwable)
                        }
                    },
                )

        return AutoCloseable {
            // Shutting down awaits termination, so the flush sees the whole stream. It runs even if
            // shutdown fails, or a failure there would take the buffered diagnostics down with it.
            try {
                server.close()
            } finally {
                _pendingDiagnostics.flush(_messageWriter)
            }
        }
    }

    private fun onError(err: Throwable) {
        _messageWriter.error("BES Server onError", err.toString())
    }

    private fun onEvent(event: BesGrpcServerEventStream.Result.Event) {
        hasStarted = true

        val messagePrefix = MessagePrefix.build(_verbosity, event.sequenceNumber, event.streamId)
        val flowId = event.streamId.invocationId.ifEmpty { event.streamId.buildId }
        val time = event.event.eventTime
        val timestamp = Date(time.seconds * 1000 + time.nanos / 1_000_000)
        // Diagnostics retain this writer. Capture only the header, not the whole protobuf event.
        val writer =
            MessageWriter(messagePrefix) { message ->
                if (message.flowId.isNullOrEmpty()) {
                    message.setFlowId(flowId)
                }
                message.setTimestamp(timestamp)
                _messageWriter.write(message)
            }
        val ctx =
            GrpcEventHandlerContext(
                _verbosity,
                event.streamId,
                event.event,
                writer,
                _reportTargetLogToBuildLog,
            )
        _buildEventHandler.handle(ctx)
    }
}
