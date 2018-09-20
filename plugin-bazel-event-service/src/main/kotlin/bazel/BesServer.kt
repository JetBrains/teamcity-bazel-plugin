package bazel

import bazel.events.OrderedBuildEvent
import bazel.messages.*
import devteam.rx.*

class BesServer<TEvent>(
        private val _gRpcServer: GRpcServer,
        private val _verbosity: Verbosity,
        private val _bindableEventService: BindableEventService<TEvent>,
        private val _buildEventConverter: Converter<Event<TEvent>, Event<OrderedBuildEvent>>)
    : Observable<String> {

    override fun subscribe(observer: Observer<String>): Disposable {
        val messageFactory = MessageFactoryImpl()
        val serviceMessageSubject = ServiceMessageRootSubject(ControllerSubject(_verbosity, messageFactory, BlockManagerImpl()) { StreamSubject(_verbosity, messageFactory, BlockManagerImpl()) })
        val buildEventSource = _bindableEventService.map { _buildEventConverter.convert(it) }.share()
        val subscription = disposableOf(
                // service messages subscription
                serviceMessageSubject.map { it.asString() }.subscribe(observer),

                // service control signals subscription
                serviceMessageSubject.subscribe({ }, { _gRpcServer.shutdown() }, { _gRpcServer.shutdown() }),

                // converting subscription
                buildEventSource.subscribe(serviceMessageSubject)
        )

        // gRpc server token
        val gRpcServerToken = _gRpcServer.start(_bindableEventService)
        return disposableOf(gRpcServerToken, subscription)
    }
}