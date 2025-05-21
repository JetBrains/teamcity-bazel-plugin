package bazel.messages

import bazel.Event
import bazel.Verbosity
import bazel.events.OrderedBuildEvent
import bazel.messages.handlers.MessageBuilderContext
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import devteam.rx.Observer
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

open class ServiceMessageContext(
    val observer: Observer<ServiceMessage>,
    val hierarchy: Hierarchy,
    override val event: Event<OrderedBuildEvent>,
    override val messageFactory: MessageFactory,
    override val verbosity: Verbosity,
) : MessageBuilderContext {
    fun onNext(value: ServiceMessage) = observer.onNext(value)
}

class BazelEventHandlerContext(
    val observer: Observer<ServiceMessage>,
    val hierarchy: Hierarchy,
    val bazelEvent: BuildEventStreamProtos.BuildEvent,
    override val event: Event<OrderedBuildEvent>,
    override val messageFactory: MessageFactory,
    override val verbosity: Verbosity,
) : MessageBuilderContext {
    fun onNext(value: ServiceMessage) = observer.onNext(value)
}
