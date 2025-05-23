package bazel.v1

import bazel.toObserver
import bazel.toStreamObserver
import com.google.devtools.build.v1.*
import com.google.protobuf.Empty
import devteam.rx.Disposable
import devteam.rx.Observable
import devteam.rx.Observer
import devteam.rx.observer
import devteam.rx.subjectOf
import io.grpc.stub.StreamObserver
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger

class PublishBuildEventService :
    PublishBuildEventGrpc.PublishBuildEventImplBase(),
    Observable<PublishBuildEventService.Event> {
    private val eventSubject = subjectOf<Event>()
    private val projectId = AtomicReference("")

    override fun subscribe(observer: Observer<Event>): Disposable = eventSubject.subscribe(observer)

    // BuildEvents are used to declare the beginning and end of major portions of a Build
    override fun publishLifecycleEvent(
        request: PublishLifecycleEventRequest?,
        responseObserver: StreamObserver<Empty>?,
    ) {
        logger.log(Level.FINE, "publishLifecycleEvent: $request")

        projectId.compareAndSet("", request?.projectId ?: "")

        if (request?.hasBuildEvent() == true) {
            eventSubject.onNext(
                Event(
                    projectId.get(),
                    request.buildEvent.sequenceNumber,
                    request.buildEvent.streamId,
                    request.buildEvent.event,
                ),
            )
        }

        responseObserver?.let {
            it.onNext(Empty.getDefaultInstance())
            it.onCompleted()
        }
    }

    // This method is used to stream detailed events from the build tool during the build (e.g., target completion, test results, actions, etc.).
    // This is a bidirectional streaming RPC, server responds to each one with an acknowledgment.
    // NOTE: only single stream can be opened during the build
    override fun publishBuildToolEventStream(
        responseObserver: StreamObserver<PublishBuildToolEventStreamResponse>?,
    ): StreamObserver<PublishBuildToolEventStreamRequest> {
        logger.log(Level.FINE, "publishBuildToolEventStream: $responseObserver")
        val responses = responseObserver?.toObserver() ?: observer(onNext = {}, onError = {}, onComplete = {})
        return PublishEventObserver(projectId.get(), responses, eventSubject).toStreamObserver()
    }

    companion object {
        private val logger = Logger.getLogger(PublishBuildEventService::class.java.name)
    }

    data class Event(
        val projectId: String,
        val sequenceNumber: Long,
        val streamId: StreamId,
        val event: BuildEvent,
    )

    private class PublishEventObserver(
        private val _projectId: String,
        private val _responseObserver: Observer<PublishBuildToolEventStreamResponse>,
        private val _eventObserver: Observer<Event>,
    ) : Observer<PublishBuildToolEventStreamRequest> {
        override fun onNext(value: PublishBuildToolEventStreamRequest) {
            logger.log(Level.FINE, "onNext: $value")

            // send response
            _responseObserver.onNext(
                PublishBuildToolEventStreamResponse
                    .newBuilder()
                    .setSequenceNumber(value.orderedBuildEventOrBuilder.sequenceNumber)
                    .setStreamId(value.orderedBuildEventOrBuilder.streamId)
                    .build(),
            )

            if (value.hasOrderedBuildEvent() && value.orderedBuildEvent.hasEvent()) {
                _eventObserver.onNext(
                    Event(
                        _projectId,
                        value.orderedBuildEvent.sequenceNumber,
                        value.orderedBuildEvent.streamId,
                        value.orderedBuildEvent.event,
                    ),
                )
            } else {
                logger.log(Level.SEVERE, "OrderedBuildEvent was not found.")
            }

            if (value.orderedBuildEvent.event.hasComponentStreamFinished()) {
                // send onCompleted
                logger.log(Level.INFO, "The ComponentStreamFinished event was received.")
                _responseObserver.onComplete()
            }
        }

        override fun onError(error: Exception) {
            logger.log(Level.SEVERE, "onError: $error")
            _eventObserver.onError(error)
        }

        override fun onComplete() {
            logger.log(Level.INFO, "onComplete")
            _eventObserver.onComplete()
        }

        companion object {
            private val logger = Logger.getLogger(PublishEventObserver::class.java.name)
        }
    }
}
