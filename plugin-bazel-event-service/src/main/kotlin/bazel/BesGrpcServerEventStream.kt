package bazel

import bazel.messages.MessageWriter
import com.google.devtools.build.v1.BuildEvent
import com.google.devtools.build.v1.PublishBuildEventGrpc
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse
import com.google.devtools.build.v1.PublishLifecycleEventRequest
import com.google.devtools.build.v1.StreamId
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver

class BesGrpcServerEventStream(
    private val messageWriter: MessageWriter,
    private val onEvent: (Result) -> Unit,
) : PublishBuildEventGrpc.PublishBuildEventImplBase() {
    sealed interface Result {
        data class Event(
            val sequenceNumber: Long,
            val streamId: StreamId,
            val event: BuildEvent,
        ) : Result

        data class Error(
            val throwable: Throwable,
        ) : Result
    }

    // BuildEvents are used to declare the beginning and end of major portions of a Build
    override fun publishLifecycleEvent(
        request: PublishLifecycleEventRequest,
        responseObserver: StreamObserver<Empty>,
    ) {
        if (request.hasBuildEvent()) {
            onEvent(
                Result.Event(
                    request.buildEvent.sequenceNumber,
                    request.buildEvent.streamId,
                    request.buildEvent.event,
                ),
            )
        }
        responseObserver.onNext(Empty.getDefaultInstance())
        responseObserver.onCompleted()
    }

    // This method is used to stream detailed events from the build tool during the build (e.g., target completion, test results, actions, etc.).
    // This is a bidirectional streaming RPC, server responds to each one with an acknowledgment.
    // NOTE: only single stream can be opened during the build
    override fun publishBuildToolEventStream(
        responseObserver: StreamObserver<PublishBuildToolEventStreamResponse>,
    ): StreamObserver<PublishBuildToolEventStreamRequest> = PublishEventObserver(messageWriter, responseObserver, onEvent)

    private class PublishEventObserver(
        private val messageWriter: MessageWriter,
        private val _responseObserver: StreamObserver<PublishBuildToolEventStreamResponse>,
        private val onEvent: (Result) -> Unit,
    ) : StreamObserver<PublishBuildToolEventStreamRequest> {
        override fun onNext(value: PublishBuildToolEventStreamRequest) {
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
                        value.orderedBuildEvent.sequenceNumber,
                        value.orderedBuildEvent.streamId,
                        value.orderedBuildEvent.event,
                    ),
                )
            } else {
                messageWriter.error("OrderedBuildEvent was not found.")
            }
        }

        override fun onError(error: Throwable) {
            messageWriter.error(error.message ?: error.toString())
            onEvent(Result.Error(error))
        }

        override fun onCompleted() = _responseObserver.onCompleted()
    }
}
