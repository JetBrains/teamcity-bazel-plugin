package bazel.messages

import bazel.Event
import bazel.Verbosity
import bazel.events.OrderedBuildEvent
import bazel.messages.handlers.EventHandler
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import devteam.rx.Observer
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

open class ServiceMessageContext(
    private val _observer: Observer<ServiceMessage>,
    val handlerIterator: Iterator<EventHandler>,
    val event: Event<OrderedBuildEvent>,
    val messageFactory: MessageFactory,
    val hierarchy: Hierarchy,
    val verbosity: Verbosity,
) : Observer<ServiceMessage> {
    override fun onNext(value: ServiceMessage) = _observer.onNext(value)

    override fun onError(error: Exception) = _observer.onError(error)

    override fun onComplete() = _observer.onComplete()
}

class BazelEventHandlerContext(
    observer: Observer<ServiceMessage>,
    handlerIterator: Iterator<EventHandler>,
    event: Event<OrderedBuildEvent>,
    messageFactory: MessageFactory,
    hierarchy: Hierarchy,
    verbosity: Verbosity,
    val bazelEvent: BuildEventStreamProtos.BuildEvent,
) : ServiceMessageContext(observer, handlerIterator, event, messageFactory, hierarchy, verbosity)
