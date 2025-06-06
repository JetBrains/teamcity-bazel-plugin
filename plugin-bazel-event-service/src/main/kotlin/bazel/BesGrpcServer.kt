package bazel

import bazel.handlers.GrpcEventHandlerChain
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.MessageFactory.createErrorMessage
import bazel.messages.MessageWriter
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.util.Date

class BesGrpcServer(
    private val _grpcServer: GrpcServer,
    private val _verbosity: Verbosity,
    private val _buildEventHandler: GrpcEventHandlerChain,
) {
    var hasStarted = false

    fun start() =
        _grpcServer
            .start(
                BesGrpcServerEventStream {
                    when (it) {
                        is BesGrpcServerEventStream.Result.Event -> onEvent(it)
                        is BesGrpcServerEventStream.Result.Error -> onError(it.throwable)
                    }
                },
            )

    private fun onError(err: Throwable) {
        val error = createErrorMessage("BES Server onError", err.toString())
        printMessage(error)
    }

    private fun onEvent(event: BesGrpcServerEventStream.Result.Event) {
        val ctx =
            GrpcEventHandlerContext(
                _verbosity,
                event.streamId,
                event.event,
                MessageWriter(_verbosity, event.sequenceNumber, event.streamId) {
                    printMessage(updateHeader(event, it))
                },
            )
        _buildEventHandler.handle(ctx)
    }

    private fun printMessage(message: ServiceMessage) = println(message.asString())

    private fun updateHeader(
        event: BesGrpcServerEventStream.Result.Event,
        message: ServiceMessage,
    ): ServiceMessage {
        if (message.flowId.isNullOrEmpty()) {
            if (event.streamId.invocationId.isNotEmpty()) {
                message.setFlowId(event.streamId.invocationId)
            } else {
                message.setFlowId(event.streamId.buildId)
            }
        }

        val time = event.event.eventTime
        val date = Date(time.seconds * 1000 + time.nanos / 1_000_000)
        message.setTimestamp(date)
        return message
    }
}
