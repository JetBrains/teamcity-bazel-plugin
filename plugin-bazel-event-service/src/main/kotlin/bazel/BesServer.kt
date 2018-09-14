package bazel

import bazel.events.OrderedBuildEvent
import bazel.messages.*
import devteam.rx.*

class BesServer<TEvent>(
        private val _port: Int,
        private val _verbosity: Verbosity,
        private val _bindableEventService: BindableEventService<TEvent>,
        private val _buildEventConverter: Converter<Event<TEvent>, Event<OrderedBuildEvent>>)
    : Observable<String> {

    override fun subscribe(observer: Observer<String>): Disposable {
        val messageFactory = MessageFactoryImpl()
        val serviceMessageSubject = ServiceMessageRootSubject(ControllerSubject(_verbosity, messageFactory, BlockManagerImpl()) { StreamSubject(_verbosity, messageFactory, BlockManagerImpl()) })
        val buildEventSource = _bindableEventService.map { _buildEventConverter.convert(it) }.share()
        val gRpcServer = GRpcServer(_port)
        val subscription = disposableOf(
                // service messages subscription
                serviceMessageSubject.map { it.asString() }.subscribe(observer),

                // service control signals subscription
                serviceMessageSubject.subscribe({ }, { gRpcServer.shutdown() }, { gRpcServer.shutdown() }),

                // converting subscription
                buildEventSource.subscribe(serviceMessageSubject)
        )

        // gRpc server token
        val gRpcServerToken = gRpcServer.start(_bindableEventService)

        observer.onNext("BES: ${gRpcServer.port}".apply(Color.Details))
        return disposableOf(gRpcServerToken, subscription)
    }
}