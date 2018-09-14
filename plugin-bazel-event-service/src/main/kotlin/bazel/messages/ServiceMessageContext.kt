package bazel.messages

import bazel.Event
import bazel.Verbosity
import bazel.events.OrderedBuildEvent
import bazel.messages.handlers.EventHandler
import devteam.rx.Observer
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class ServiceMessageContext(
        private val _observer: Observer<ServiceMessage>,
        val handlerIterator: Iterator<EventHandler>,
        val event: Event<OrderedBuildEvent>,
        val messageFactory: MessageFactory,
        val blockManager: BlockManager,
        val verbosity: Verbosity): Observer<ServiceMessage> {
    override fun onNext(value: ServiceMessage) = _observer.onNext(value)

    override fun onError(error: Exception) = _observer.onError(error)

    override fun onCompleted() = _observer.onCompleted()
}