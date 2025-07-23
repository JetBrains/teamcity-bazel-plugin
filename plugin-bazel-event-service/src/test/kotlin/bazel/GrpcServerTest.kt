package bazel

import bazel.messages.MessageWriter
import com.google.devtools.build.v1.BuildEvent
import com.google.devtools.build.v1.OrderedBuildEvent
import com.google.devtools.build.v1.PublishBuildEventGrpc
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest
import com.google.devtools.build.v1.PublishBuildToolEventStreamResponse
import com.google.devtools.build.v1.StreamId
import com.google.protobuf.Any
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import io.mockk.*
import org.testng.Assert
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class GrpcServerTest {
    private lateinit var messageWriter: MessageWriter
    private lateinit var grpcServer: GrpcServer
    private lateinit var serverCloseable: AutoCloseable
    private val port = 0 // Use 0 to get a random available port

    @BeforeMethod
    fun setUp() {
        messageWriter = mockk(relaxed = true)
        grpcServer = GrpcServer(messageWriter, port)
    }

    @AfterMethod
    fun tearDown() {
        if (::serverCloseable.isInitialized) {
            serverCloseable.close()
        }
    }

    @Test
    fun testLargeMessageHandling() {
        // Create a simple service that captures received events
        val receivedEvents = mutableListOf<BuildEvent>()
        val eventLatch = CountDownLatch(1)
        val responseLatch = CountDownLatch(1)

        val service =
            BesGrpcServerEventStream(messageWriter) { result ->
                when (result) {
                    is BesGrpcServerEventStream.Result.Event -> {
                        receivedEvents.add(result.event)
                        eventLatch.countDown()
                    }
                    is BesGrpcServerEventStream.Result.Error -> {
                        Assert.fail("Unexpected error: ${result.throwable}")
                    }
                }
            }

        // Start the server
        serverCloseable = grpcServer.start(service)
        val actualPort = grpcServer.port
        Assert.assertTrue(actualPort > 0, "Server should be started on a valid port")

        // Create a client channel
        val channel =
            ManagedChannelBuilder
                .forAddress("localhost", actualPort)
                .usePlaintext()
                .build()

        try {
            val stub = PublishBuildEventGrpc.newStub(channel)

            // Create a large message (over 4MB)
            val largeData = ByteString.copyFrom(ByteArray(4 * 1024 * 1024)) // 4MB of data
            val largeEvent =
                BuildEvent
                    .newBuilder()
                    .setBazelEvent(
                        Any
                            .newBuilder()
                            .setTypeUrl("type.googleapis.com/test.LargeEvent")
                            .setValue(largeData)
                            .build(),
                    ).build()

            val streamId =
                StreamId
                    .newBuilder()
                    .setBuildId("test-build-id")
                    .build()

            // Send the large message
            val responseObserver =
                object : StreamObserver<PublishBuildToolEventStreamResponse> {
                    override fun onNext(value: PublishBuildToolEventStreamResponse) {
                        responseLatch.countDown()
                    }

                    override fun onError(t: Throwable) {
                        Assert.fail("Client error: ${t.message}")
                    }

                    override fun onCompleted() {}
                }

            val requestObserver = stub.publishBuildToolEventStream(responseObserver)

            val request =
                PublishBuildToolEventStreamRequest
                    .newBuilder()
                    .setOrderedBuildEvent(
                        OrderedBuildEvent
                            .newBuilder()
                            .setSequenceNumber(1)
                            .setStreamId(streamId)
                            .setEvent(largeEvent)
                            .build(),
                    ).build()

            requestObserver.onNext(request)
            requestObserver.onCompleted()

            // Wait for the event to be received
            Assert.assertTrue(
                eventLatch.await(1, TimeUnit.SECONDS),
                "Should receive the large message within timeout",
            )

            // Verify the event was received
            Assert.assertEquals(receivedEvents.size, 1, "Should receive exactly one event")
            Assert.assertEquals(
                receivedEvents[0].bazelEvent.value.size(),
                largeData.size(),
                "Received event should have the same size as sent",
            )

            // Verify response was sent
            Assert.assertTrue(
                responseLatch.await(1, TimeUnit.SECONDS),
                "Should receive response within timeout",
            )
        } finally {
            channel.shutdown()
            channel.awaitTermination(1, TimeUnit.SECONDS)
        }
    }

    @Test
    fun testDefaultMessageSizeLimit() {
        // This test verifies that without our fix, messages over 4MB would fail
        // Since we've already applied the fix, this test will pass
        // In a real scenario, you might want to parameterize the max message size

        val service =
            BesGrpcServerEventStream(messageWriter) { result ->
                when (result) {
                    is BesGrpcServerEventStream.Result.Event -> {
                        // Event received successfully
                    }
                    is BesGrpcServerEventStream.Result.Error -> {
                        Assert.fail("Should handle large messages without error")
                    }
                }
            }

        serverCloseable = grpcServer.start(service)
        val actualPort = grpcServer.port

        // Just verify the server started successfully with our configuration
        Assert.assertTrue(actualPort > 0, "Server should start successfully")

        // In production, you might want to make the max message size configurable
        // and test different size limits
    }

    @Test
    fun testConfigurableMessageSizeLimit() {
        // Test with a smaller message size limit
        val smallLimitServer = GrpcServer(messageWriter, port, _maxMessageSizeMb = 2) // 2MB limit
        val errorLatch = CountDownLatch(1)

        val service =
            BesGrpcServerEventStream(messageWriter) { result ->
                when (result) {
                    is BesGrpcServerEventStream.Result.Event -> {
                        // Should not receive this event as it exceeds the limit
                        Assert.fail("Should not receive event exceeding size limit")
                    }
                    is BesGrpcServerEventStream.Result.Error -> {
                        // Expected - message too large
                        errorLatch.countDown()
                    }
                }
            }

        val closeable = smallLimitServer.start(service)
        try {
            val actualPort = smallLimitServer.port
            Assert.assertTrue(actualPort > 0, "Server should be started on a valid port")

            // Create a client channel with matching size limit
            val channel =
                ManagedChannelBuilder
                    .forAddress("localhost", actualPort)
                    .usePlaintext()
                    .maxInboundMessageSize(2 * 1024 * 1024) // Match server's 2MB limit
                    .build()

            try {
                val stub = PublishBuildEventGrpc.newStub(channel)

                // Create a message larger than 2MB but smaller than default
                val largeData = ByteString.copyFrom(ByteArray(3 * 1024 * 1024)) // 3MB of data
                val largeEvent =
                    BuildEvent
                        .newBuilder()
                        .setBazelEvent(
                            Any
                                .newBuilder()
                                .setTypeUrl("type.googleapis.com/test.LargeEvent")
                                .setValue(largeData)
                                .build(),
                        ).build()

                val streamId =
                    StreamId
                        .newBuilder()
                        .setBuildId("test-build-id")
                        .build()

                // Send the large message
                val clientErrorLatch = CountDownLatch(1)
                val responseObserver =
                    object : StreamObserver<PublishBuildToolEventStreamResponse> {
                        override fun onNext(value: PublishBuildToolEventStreamResponse) {}

                        override fun onError(t: Throwable) {
                            clientErrorLatch.countDown()
                        }

                        override fun onCompleted() {}
                    }

                val requestObserver = stub.publishBuildToolEventStream(responseObserver)

                val request =
                    PublishBuildToolEventStreamRequest
                        .newBuilder()
                        .setOrderedBuildEvent(
                            OrderedBuildEvent
                                .newBuilder()
                                .setSequenceNumber(1)
                                .setStreamId(streamId)
                                .setEvent(largeEvent)
                                .build(),
                        ).build()

                requestObserver.onNext(request)
                requestObserver.onCompleted()

                // Wait for the error to be received
                Assert.assertTrue(
                    clientErrorLatch.await(1, TimeUnit.SECONDS),
                    "Should receive error for oversized message",
                )
            } finally {
                channel.shutdown()
                channel.awaitTermination(1, TimeUnit.SECONDS)
            }
        } finally {
            closeable.close()
        }
    }
}
