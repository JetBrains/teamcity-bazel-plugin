package bazel

import bazel.messages.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.util.Date

class BesServer(
    private val port: Int,
    private val _verbosity: Verbosity,
    private val _messageFactory: MessageFactory,
    private val _hierarchy: Hierarchy,
    private val _buildEventHandler: RootBuildEventHandler,
) {
    var hasStarted = false

    fun start() =
        GRpcServer(port)
            .start(
                PublishBuildEventService {
                    when (it) {
                        is PublishBuildEventService.Result.Event -> onEvent(it)
                        is PublishBuildEventService.Result.Error -> onError(it.throwable)
                    }
                },
            )

    private fun onError(err: Throwable) {
        val error = _messageFactory.createErrorMessage("BES Server onError", err.toString())
        printMessage(error)
    }

    private fun onEvent(event: PublishBuildEventService.Result.Event) {
        val ctx =
            BuildEventHandlerContext(
                _messageFactory,
                _hierarchy,
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
                printMessage(_messageFactory.createTraceMessage(ctx.event.toString()))
            }
        }
    }

    private fun printMessage(message: ServiceMessage) {
        println(message.asString())
    }

    private fun updateHeader(
        event: PublishBuildEventService.Result.Event,
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
