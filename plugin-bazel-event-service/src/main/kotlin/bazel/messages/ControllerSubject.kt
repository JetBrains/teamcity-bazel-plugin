

package bazel.messages

import bazel.Event
import bazel.Verbosity
import bazel.atLeast
import bazel.events.InvocationAttemptFinished
import bazel.events.OrderedBuildEvent
import bazel.messages.handlers.*
import devteam.rx.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import java.util.logging.Logger

class ControllerSubject(
        private val _verbosity: Verbosity,
        private val _messageFactory: MessageFactory,
        private val _hierarchy: Hierarchy,
        private val _streamSubjectFactory: () -> ServiceMessageSubject)
    : ServiceMessageSubject {
    private val _controllerSubject = subjectOf<ServiceMessage>()
    private val _streams = mutableMapOf<String, Stream>()
    private val _disposed: AtomicBoolean = AtomicBoolean()

    override fun onNext(value: Event<OrderedBuildEvent>) {
        if (_disposed.get()) {
            return
        }

        val invocationId = value.payload.streamId.invocationId
        val handlerIterator = handlers.iterator()

        // this subject is needed to wrap all onNext calls from handlers with updateHeader method
        val subject = subjectOf<ServiceMessage>()
        val ctx = ServiceMessageContext(subject, handlerIterator, value, _messageFactory, _hierarchy, _verbosity)
        subject.subscribe(observer(
            onNext = { _controllerSubject.onNext(updateHeader(value.payload, it)) },
            onError = { _controllerSubject.onError(it) },
            onComplete = { _controllerSubject.onComplete() }
        )).use {
            val processed = handlerIterator.next().handle(ctx)

            if (processed) {
                if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                    subject.onNext(_messageFactory.createTraceMessage(value.payload.toString()))
                }

                if (value.payload is InvocationAttemptFinished) {
                    _streams.remove(invocationId)
                }

                return
            }
        }

        _streams.getOrPut(value.payload.streamId.invocationId) { createStreamSubject() }.subject.onNext(value)
    }

    override fun onError(error: Exception) = _controllerSubject.onError(error)

    override fun onComplete() {
        logger.log(Level.INFO, "onComplete")
    }

    override fun subscribe(observer: Observer<ServiceMessage>): Disposable = _controllerSubject.subscribe(observer)

    override fun dispose() {
        if (_disposed.compareAndSet(false, true)) {
            for (stream in _streams.values) {
                stream.dispose()
            }
        }
    }

    private fun createStreamSubject(): Stream {
        val newStreamSubject = _streamSubjectFactory()
        return Stream(newStreamSubject, newStreamSubject.subscribe(_controllerSubject))
    }

    private fun updateHeader(event: OrderedBuildEvent, message: ServiceMessage): ServiceMessage {
        if (message.flowId.isNullOrEmpty()) {
            message.setFlowId(event.streamId.buildId)
        }

        message.setTimestamp(event.eventTime.date)
        return message
    }

    private class Stream(
            val subject: ServiceMessageSubject,
            private val _subscription: Disposable) : Disposable {
        override fun dispose() {
            _subscription.dispose()
            subject.dispose()
        }
    }

    companion object {
        private val handlers = sequenceOf(
                BuildEnqueuedHandler(),
                InvocationAttemptStartedHandler(),
                InvocationAttemptFinishedHandler(),
                BuildFinishedHandler(),
                ComponentStreamFinishedHandler(),
                NotProcessedEventHandler()
        ).sortedBy { it.priority }.toList()
        private val logger = Logger.getLogger(ControllerSubject::class.java.name)
    }
}