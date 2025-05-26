package bazel

import com.google.devtools.build.v1.BuildEvent
import com.google.devtools.build.v1.PublishBuildEventGrpc
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse
import com.google.devtools.build.v1.PublishLifecycleEventRequest
import com.google.devtools.build.v1.StreamId
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger

class PublishBuildEventService(
    private val onEvent: (Result) -> Unit,
) : PublishBuildEventGrpc.PublishBuildEventImplBase() {
    sealed interface Result {
        data class Event(
            val projectId: String,
            val sequenceNumber: Long,
            val streamId: StreamId,
            val event: BuildEvent,
        ) : Result

        data class Error(
            val throwable: Throwable,
        ) : Result
    }

    private val projectId = AtomicReference("")

    // BuildEvents are used to declare the beginning and end of major portions of a Build
    override fun publishLifecycleEvent(
        request: PublishLifecycleEventRequest?,
        responseObserver: StreamObserver<Empty>?,
    ) {
        logger.log(Level.FINE, "publishLifecycleEvent: $request")

        projectId.compareAndSet("", request?.projectId ?: "")

        if (request?.hasBuildEvent() == true) {
            onEvent(
                Result.Event(
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
        responseObserver: StreamObserver<PublishBuildToolEventStreamResponse>,
    ): StreamObserver<PublishBuildToolEventStreamRequest> {
        logger.log(Level.FINE, "publishBuildToolEventStream: $responseObserver")
        return PublishEventObserver(projectId.get(), responseObserver, onEvent)
    }

    companion object {
        private val logger = Logger.getLogger(PublishBuildEventService::class.java.name)
    }

    private class PublishEventObserver(
        private val _projectId: String,
        private val _responseObserver: StreamObserver<PublishBuildToolEventStreamResponse>,
        private val onEvent: (Result) -> Unit,
    ) : StreamObserver<PublishBuildToolEventStreamRequest> {
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
                onEvent(
                    Result.Event(
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
                _responseObserver.onCompleted()
            }
        }

        override fun onError(error: Throwable) {
            logger.log(Level.SEVERE, "onError: $error")
            onEvent(Result.Error(error))
        }

        override fun onCompleted() {
            logger.log(Level.INFO, "onComplete")
        }

        companion object {
            private val logger = Logger.getLogger(PublishEventObserver::class.java.name)
        }
    }
}
