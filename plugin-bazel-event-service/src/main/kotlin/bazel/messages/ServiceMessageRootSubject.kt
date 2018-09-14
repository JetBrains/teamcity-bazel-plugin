package bazel.messages

import bazel.Event
import bazel.events.BuildFinished
import bazel.events.OrderedBuildEvent
import devteam.rx.Disposable
import devteam.rx.Observer
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class ServiceMessageRootSubject(private val _streamsSubject: ServiceMessageSubject): ServiceMessageSubject {
    override fun onNext(value: Event<OrderedBuildEvent>) {
        _streamsSubject.onNext(value)

        if (value.payload is BuildFinished) {
            onCompleted()
        }
    }

    override fun onError(error: Exception) = _streamsSubject.onError(error)

    override fun onCompleted() = _streamsSubject.onCompleted()

    override fun subscribe(observer: Observer<ServiceMessage>): Disposable = _streamsSubject.subscribe(observer)

    override fun dispose() = _streamsSubject.dispose()
}