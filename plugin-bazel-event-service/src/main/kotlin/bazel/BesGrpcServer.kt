package bazel

import bazel.handlers.GrpcEventHandlerChain
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.util.Date

class BesGrpcServer(
    private val grpcServer: GrpcServer,
    private val _verbosity: Verbosity,
    private val _buildEventHandler: GrpcEventHandlerChain,
) {
    var hasStarted = false

    fun start() =
        grpcServer
            .start(
                BesGrpcServerEventStream {
                    when (it) {
                        is BesGrpcServerEventStream.Result.Event -> onEvent(it)
                        is BesGrpcServerEventStream.Result.Error -> onError(it.throwable)
                    }
                },
            )

    private fun onError(err: Throwable) {
        val error = MessageFactory.createErrorMessage("BES Server onError", err.toString())
        printMessage(error)
    }

    private fun onEvent(event: BesGrpcServerEventStream.Result.Event) {
        val ctx =
            GrpcEventHandlerContext(
                _verbosity,
                event.sequenceNumber,
                event.streamId,
                event.event,
            ) { serviceMessage ->
                hasStarted = true
                printMessage(updateHeader(event, serviceMessage))
            }
        val processed = _buildEventHandler.handle(ctx)
        if (processed) {
            if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                printMessage(MessageFactory.createTraceMessage(ctx.event.toString()))
            }
        }
    }

    private fun printMessage(message: ServiceMessage) {
        println(message.asString())
    }

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
