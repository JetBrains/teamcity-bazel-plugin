package bazel

import bazel.messages.*
import bazel.v1.PublishBuildEventService
import devteam.rx.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.util.Date

class BesServer(
    private val _gRpcServer: GRpcServer,
    private val _verbosity: Verbosity,
    private val _bindableEventService: PublishBuildEventService,
    private val _messageFactory: MessageFactory,
    private val _hierarchy: Hierarchy,
    private val _buildEventHandler: RootBuildEventHandler,
) : Observable<String> {
    override fun subscribe(observer: Observer<String>): Disposable {
        val subscription =
            _bindableEventService.subscribe(
                observer(
                    onNext = {
                        val ctx =
                            BuildEventHandlerContext(
                                _messageFactory,
                                _hierarchy,
                                _verbosity,
                                it.sequenceNumber,
                                it.projectId,
                                it.streamId,
                                it.event,
                            ) { serviceMessage ->
                                observer.onNext(updateHeader(it, serviceMessage).asString())
                            }
                        val processed = _buildEventHandler.handle(ctx)
                        if (processed) {
                            if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                                ctx.onNext(_messageFactory.createTraceMessage(ctx.event.toString()))
                            }
                        }
                    },
                    onError = { observer.onError(it) },
                    onComplete = { observer.onComplete() },
                ),
            )

        // gRpc server token
        val gRpcServerToken = _gRpcServer.start(_bindableEventService)
        return disposableOf(gRpcServerToken, subscription)
    }

    private fun updateHeader(
        event: PublishBuildEventService.Event,
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
