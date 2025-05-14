

package bazel

import bazel.events.OrderedBuildEvent
import bazel.messages.*
import bazel.v1.PublishBuildEventService
import devteam.rx.*

class BesServer(
    private val _gRpcServer: GRpcServer,
    private val _verbosity: Verbosity,
    private val _bindableEventService: PublishBuildEventService,
    private val _buildEventConverter: Converter<Event<com.google.devtools.build.v1.OrderedBuildEvent>, Event<OrderedBuildEvent>>,
    private val _messageFactory: MessageFactory,
) : Observable<String> {
    override fun subscribe(observer: Observer<String>): Disposable {
        val controllerSubject =
            ControllerSubject(_verbosity, _messageFactory, Hierarchy()) {
                StreamSubject(
                    _verbosity,
                    _messageFactory,
                    Hierarchy(),
                )
            }
        val subscription =
            disposableOf(
                // service messages subscription
                controllerSubject.subscribe(
                    observer(
                        onNext = { observer.onNext(it.asString()) },
                        onError = {
                            observer.onError(it)
                            _gRpcServer.shutdown()
                        },
                        onComplete = {
                            observer.onComplete()
                            _gRpcServer.shutdown()
                        },
                    ),
                ),
                // converting subscription
                _bindableEventService.subscribe(
                    observer(
                        onNext = { controllerSubject.onNext(_buildEventConverter.convert(it)) },
                        onError = { controllerSubject.onError(it) },
                        onComplete = { controllerSubject.onComplete() },
                    ),
                ),
            )

        // gRpc server token
        val gRpcServerToken = _gRpcServer.start(_bindableEventService)
        return disposableOf(gRpcServerToken, subscription)
    }
}
