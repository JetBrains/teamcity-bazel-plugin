package bazel.messages

import bazel.Event
import bazel.Verbosity
import bazel.atLeast
import bazel.events.StreamId
import bazel.events.Timestamp
import bazel.messages.handlers.*
import bazel.v1.converters.BuildComponentConverter
import bazel.v1.converters.StreamIdConverter
import com.google.devtools.build.v1.OrderedBuildEvent
import devteam.rx.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level
import java.util.logging.Logger

class ControllerSubject(
    private val verbosity: Verbosity,
    private val messageFactory: MessageFactory,
    private val hierarchy: Hierarchy,
) : Observer<Event<OrderedBuildEvent>>, Observable<ServiceMessage> {
    private val controllerSubject = subjectOf<ServiceMessage>()
    private val disposed: AtomicBoolean = AtomicBoolean()
    private val streamIdConverter = StreamIdConverter(BuildComponentConverter())

    override fun onNext(value: Event<OrderedBuildEvent>) {
        if (disposed.get()) {
            return
        }

        if (value.payload.hasEvent()) {
            logger.log(Level.SEVERE, "Unknown event: $value")
        }

        val payload = value.payload
        val streamId = if (payload.hasStreamId()) streamIdConverter.convert(payload.streamId) else StreamId.default
        val sequenceNumber = payload.sequenceNumber
        val eventTime = Timestamp(payload.event.eventTime.seconds, payload.event.eventTime.nanos)
        val convertedEvent =
            Event<bazel.events.OrderedBuildEvent>(value.projectId, object : bazel.events.OrderedBuildEvent {
                override val streamId = streamId
                override val sequenceNumber = sequenceNumber
                override val eventTime = eventTime
            }, value.payload.event)

        // this subject is needed to wrap all onNext calls from handlers with updateHeader method
        val subject = subjectOf<ServiceMessage>()
        val ctx = ServiceMessageContext(subject, hierarchy, convertedEvent, messageFactory, verbosity)
        subject
            .subscribe(
                observer(
                    onNext = { controllerSubject.onNext(updateHeader(convertedEvent.payload, it)) },
                    onError = { controllerSubject.onError(it) },
                    onComplete = { controllerSubject.onComplete() },
                ),
            ).use {
                val processed = handlers.firstOrNull { it.handle(ctx) } != null

                if (processed) {
                    if (verbosity.atLeast(Verbosity.Diagnostic)) {
                        subject.onNext(messageFactory.createTraceMessage(convertedEvent.payload.toString()))
                    }
                    return
                }
            }
    }

    override fun onError(error: Exception) = controllerSubject.onError(error)

    override fun onComplete() {
        logger.log(Level.INFO, "onComplete")
    }

    override fun subscribe(observer: Observer<ServiceMessage>): Disposable = controllerSubject.subscribe(observer)

    private fun updateHeader(
        event: bazel.events.OrderedBuildEvent,
        message: ServiceMessage,
    ): ServiceMessage {
        if (message.flowId.isNullOrEmpty()) {
            if (event.streamId.invocationId.isNotEmpty()) {
                message.setFlowId(event.streamId.invocationId)
            } else {
                message.setFlowId(event.streamId.buildId)
            }
        }

        message.setTimestamp(event.eventTime.date)
        return message
    }

    companion object {
        private val handlers =
            listOf(
                BuildEnqueuedHandler(),
                InvocationAttemptStartedHandler(),
                InvocationAttemptFinishedHandler(),
                RootBazelEventHandler(),
                BuildFinishedHandler(),
                ComponentStreamFinishedHandler(),
                ConsoleOutputHandler(),
                NotProcessedEventHandler(),
            )
        private val logger = Logger.getLogger(ControllerSubject::class.java.name)
    }
}
