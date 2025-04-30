

package bazel.v1

import bazel.BindableEventService
import bazel.Event
import bazel.toObserver
import bazel.toStreamObserver
import com.google.devtools.build.v1.*
import com.google.protobuf.Empty
import devteam.rx.Disposable
import devteam.rx.Observer
import devteam.rx.observer
import devteam.rx.subjectOf
import io.grpc.stub.StreamObserver
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger

internal class PublishBuildEventService
    : PublishBuildEventGrpc.PublishBuildEventImplBase(), BindableEventService<OrderedBuildEvent> {

    private val _eventSubject = subjectOf<Event<OrderedBuildEvent>>()
    private val _projectId = AtomicReference<String>("")

    override fun subscribe(observer: Observer<Event<OrderedBuildEvent>>): Disposable {
        return _eventSubject.subscribe(observer)
    }

    override fun publishLifecycleEvent(request: PublishLifecycleEventRequest?, responseObserver: StreamObserver<Empty>?) {
        logger.log(Level.FINE, "publishLifecycleEvent: $request")

        _projectId.compareAndSet("", request?.projectId ?: "")

        if (request?.hasBuildEvent() == true) {
            _eventSubject.onNext(Event(_projectId.get(), request.buildEvent))
        }

        responseObserver?.let {
            it.onNext(Empty.getDefaultInstance())
            it.onCompleted()
        }
    }

    override fun publishBuildToolEventStream(responseObserver: StreamObserver<PublishBuildToolEventStreamResponse>?): StreamObserver<PublishBuildToolEventStreamRequest> {
        logger.log(Level.FINE, "publishBuildToolEventStream: $responseObserver")
        val responses = responseObserver?.toObserver() ?: observer(onNext = {}, onError = {}, onComplete = {})
        return PublishEventObserver(_projectId.get(), responses, _eventSubject).toStreamObserver()
    }

    companion object {
        private val logger = Logger.getLogger(PublishBuildEventService::class.java.name)
    }

    private class PublishEventObserver(
            private val _projectId: String,
            private val _responseObserver: Observer<PublishBuildToolEventStreamResponse>,
            private val _eventObserver: Observer<Event<OrderedBuildEvent>>)
        : Observer<PublishBuildToolEventStreamRequest> {

        override fun onNext(value: PublishBuildToolEventStreamRequest) {
            logger.log(Level.FINE, "onNext: $value")

            // send response
            _responseObserver.onNext(
                    PublishBuildToolEventStreamResponse.newBuilder()
                            .setSequenceNumber(value.orderedBuildEventOrBuilder.sequenceNumber)
                            .setStreamId(value.orderedBuildEventOrBuilder.streamId)
                            .build())

            if (value.hasOrderedBuildEvent()) {
                _eventObserver.onNext(Event(_projectId, value.orderedBuildEvent))
            } else {
                logger.log(Level.SEVERE, "OrderedBuildEvent was not found.")
            }

            if (value.orderedBuildEvent.event.hasComponentStreamFinished()) {
                // send onCompleted
                logger.log(Level.FINE, "The ComponentStreamFinished event was received.")
                _responseObserver.onComplete()
            }
        }

        override fun onError(error: Exception) {
            logger.log(Level.SEVERE, "onError: $error")
            _eventObserver.onError(error)
        }

        override fun onComplete() {
            logger.log(Level.FINE, "onComplete")
            _eventObserver.onComplete()
        }

        companion object {
            private val logger = Logger.getLogger(PublishEventObserver::class.java.name)
        }
    }
}