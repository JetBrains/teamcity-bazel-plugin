

package bazel

import bazel.events.OrderedBuildEvent
import bazel.messages.*
import devteam.rx.*

class BesServer<TEvent>(
        private val _gRpcServer: GRpcServer,
        private val _verbosity: Verbosity,
        private val _bindableEventService: BindableEventService<TEvent>,
        private val _buildEventConverter: Converter<Event<TEvent>, Event<OrderedBuildEvent>>,
        private val _messageFactory: MessageFactory)
    : Observable<String> {

    override fun subscribe(observer: Observer<String>): Disposable {
        val controllerSubject = ControllerSubject(_verbosity, _messageFactory, HierarchyImpl()) {
            StreamSubject(
                _verbosity,
                _messageFactory,
                HierarchyImpl()
            )
        }
        val subscription = disposableOf(
            // service messages subscription
            controllerSubject.subscribe(observer(
                onNext = { observer.onNext(it.asString()) },
                onError = { observer.onError(it) },
                onComplete = { observer.onComplete() }
            )),

            // service control signals subscription
            controllerSubject.subscribe(observer(
                onNext = { },
                onError = { _ -> _gRpcServer.shutdown() },
                onComplete = { _gRpcServer.shutdown() })
            ),

            // converting subscription
            _bindableEventService.subscribe(observer(
                onNext = { controllerSubject.onNext(_buildEventConverter.convert(it)) },
                onError = { controllerSubject.onError(it) },
                onComplete = { controllerSubject.onComplete() }
            ))
        )

        // gRpc server token
        val gRpcServerToken = _gRpcServer.start(_bindableEventService)
        return disposableOf(gRpcServerToken, subscription)
    }
}