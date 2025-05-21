package bazel.messages

import bazel.Event
import bazel.Verbosity
import bazel.events.OrderedBuildEvent
import bazel.messages.handlers.EventHandler
import bazel.messages.handlers.MessageBuilderContext
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import devteam.rx.Observer
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

open class ServiceMessageContext(
    private val _observer: Observer<ServiceMessage>,
    val handlerIterator: Iterator<EventHandler>,
    val hierarchy: Hierarchy,
    override val event: Event<OrderedBuildEvent>,
    override val messageFactory: MessageFactory,
    override val verbosity: Verbosity,
) : Observer<ServiceMessage>,
    MessageBuilderContext {
    override fun onNext(value: ServiceMessage) = _observer.onNext(value)

    override fun onError(error: Exception) = _observer.onError(error)

    override fun onComplete() = _observer.onComplete()
}

class BazelEventHandlerContext(
    private val _observer: Observer<ServiceMessage>,
    val hierarchy: Hierarchy,
    val bazelEvent: BuildEventStreamProtos.BuildEvent,
    override val event: Event<OrderedBuildEvent>,
    override val messageFactory: MessageFactory,
    override val verbosity: Verbosity,
) : MessageBuilderContext {
    fun onNext(value: ServiceMessage) = _observer.onNext(value)
}
