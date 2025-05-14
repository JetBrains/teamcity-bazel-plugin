

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
    private val verbosity: Verbosity,
    private val messageFactory: MessageFactory,
    private val hierarchy: Hierarchy,
    private val streamSubjectFactory: () -> ServiceMessageSubject,
) : ServiceMessageSubject {
    private val controllerSubject = subjectOf<ServiceMessage>()
    private val streams = mutableMapOf<String, Stream>()
    private val disposed: AtomicBoolean = AtomicBoolean()

    override fun onNext(value: Event<OrderedBuildEvent>) {
        if (disposed.get()) {
            return
        }

        val invocationId = value.payload.streamId.invocationId
        val handlerIterator = handlers.iterator()

        // this subject is needed to wrap all onNext calls from handlers with updateHeader method
        val subject = subjectOf<ServiceMessage>()
        val ctx = ServiceMessageContext(subject, handlerIterator, value, messageFactory, hierarchy, verbosity)
        subject
            .subscribe(
                observer(
                    onNext = { controllerSubject.onNext(updateHeader(value.payload, it)) },
                    onError = { controllerSubject.onError(it) },
                    onComplete = { controllerSubject.onComplete() },
                ),
            ).use {
                val processed = handlerIterator.next().handle(ctx)

                if (processed) {
                    if (verbosity.atLeast(Verbosity.Diagnostic)) {
                        subject.onNext(messageFactory.createTraceMessage(value.payload.toString()))
                    }

                    if (value.payload is InvocationAttemptFinished) {
                        streams.remove(invocationId)
                    }

                    return
                }
            }

        streams.getOrPut(value.payload.streamId.invocationId) { createStreamSubject() }.subject.onNext(value)
    }

    override fun onError(error: Exception) = controllerSubject.onError(error)

    override fun onComplete() {
        logger.log(Level.INFO, "onComplete")
    }

    override fun subscribe(observer: Observer<ServiceMessage>): Disposable {
        val subscription = controllerSubject.subscribe(observer)

        controllerSubject.onNext(messageFactory.createFlowStarted(FLOW_ID, ""))
        controllerSubject.onNext(messageFactory.createBlockOpened("bazel", "events stream").also { it.setFlowId(FLOW_ID) })


        return disposableOf {
            controllerSubject.onNext(messageFactory.createBlockClosed("bazel").also { it.setFlowId(FLOW_ID) })
            controllerSubject.onNext(messageFactory.createFlowFinished(FLOW_ID))
            subscription.dispose()
        }
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            for (stream in streams.values) {
                stream.dispose()
            }
        }
    }

    private fun createStreamSubject(): Stream {
        val newStreamSubject = streamSubjectFactory()
        return Stream(newStreamSubject, newStreamSubject.subscribe(controllerSubject))
    }

    private fun updateHeader(
        event: OrderedBuildEvent,
        message: ServiceMessage,
    ): ServiceMessage {
//        if (message.flowId.isNullOrEmpty()) {
//            message.setFlowId(event.streamId.buildId)
//        }
        message.setFlowId(FLOW_ID)

        message.setTimestamp(event.eventTime.date)
        return message
    }

    private class Stream(
        val subject: ServiceMessageSubject,
        private val _subscription: Disposable,
    ) : Disposable {
        override fun dispose() {
            _subscription.dispose()
            subject.dispose()
        }
    }

    companion object {
        private val handlers: List<EventHandler> =
            sequenceOf(
                BuildEnqueuedHandler(),
                InvocationAttemptStartedHandler(),
                InvocationAttemptFinishedHandler(),
                BuildFinishedHandler(),
                ComponentStreamFinishedHandler(),
                NotProcessedEventHandler(),
            ).sortedBy { it.priority }.toList()
        private val logger = Logger.getLogger(ControllerSubject::class.java.name)
        private const val FLOW_ID = "events"
    }
}
