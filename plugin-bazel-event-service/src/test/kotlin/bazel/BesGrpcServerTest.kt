package bazel

import bazel.handlers.BuildEventHandlerChain
import bazel.handlers.GrpcEventHandlerChain
import bazel.messages.MessageWriter
import bazel.messages.PendingInvocationDiagnostics
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.devtools.build.v1.BuildEvent
import com.google.devtools.build.v1.BuildStatus
import com.google.devtools.build.v1.OrderedBuildEvent
import com.google.devtools.build.v1.PublishBuildToolEventStreamRequest
import com.google.devtools.build.v1.PublishLifecycleEventRequest
import com.google.devtools.build.v1.StreamId
import com.google.protobuf.Any
import io.grpc.BindableService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class BesGrpcServerTest {
    private val messages = mutableListOf<ServiceMessage>()
    private lateinit var service: BesGrpcServerEventStream
    private lateinit var server: BesGrpcServer

    @BeforeMethod
    fun setUp() {
        unmockkAll()
        messages.clear()
    }

    @Test
    fun isNotStartedBeforeAnyEventArrives() {
        startServer().use {
            assertFalse(server.hasStarted, "No event has been published yet, got: $messages")
        }
    }

    @Test
    fun isStartedOnceAnEventArrives() {
        startServer().use {
            lifecycle("A", 1) { buildEnqueued = BuildEvent.BuildEnqueued.getDefaultInstance() }
            assertTrue(server.hasStarted, "An event was published, got: $messages")
        }
    }

    @Test
    fun preservesDiagnosticHeadersAndClosesTheFlowAfterShutdownFlush() {
        startServer().use {
            failedInvocation("A", exitCode = 39)
            assertFalse(messages.any { it.messageName == "compilationStarted" || it.messageName == "flowFinished" })
        }

        assertCompilationInOriginalFlow()
        val failure = messages.single { it.attributes["text"]?.contains("Build failed") == true }
        assertEquals(failure.flowId, "A")
        assertEquals(failure.creationTimestamp?.timestamp?.time, 3_000L)
    }

    @Test
    fun keepsEarlierDiagnosticsInTheirOwnFlowWhenAnotherBuildStarts() {
        startServer().use {
            failedInvocation("A", exitCode = 1)
            startInvocation("B")
            assertCompilationInOriginalFlow()
        }
    }

    @Test
    fun closesEveryFlowWhenInvocationsOverlap() {
        startServer().use {
            startInvocation("A")
            startInvocation("B")
            lifecycle("A", 4) { invocationAttemptFinishedBuilder.invocationStatusBuilder.result = BuildStatus.Result.COMMAND_SUCCEEDED }
            lifecycle("B", 5) { invocationAttemptFinishedBuilder.invocationStatusBuilder.result = BuildStatus.Result.COMMAND_SUCCEEDED }
        }

        val finished = messages.filter { it.messageName == "flowFinished" }
        assertEquals(finished.map { it.flowId }, listOf("A", "B"))
        assertEquals(finished.map { it.creationTimestamp?.timestamp?.time }, listOf(4_000L, 5_000L))
    }

    @Test
    fun closesASupersededFlowWithoutReportingItsCompilationErrors() {
        startServer().use {
            failedInvocation("A", exitCode = 39)
            startInvocation("B")
        }

        assertFalse(messages.any { it.messageName == "compilationStarted" })
        val flowFinished = messages.single { it.messageName == "flowFinished" }
        assertEquals(flowFinished.flowId, "A")
        assertEquals(flowFinished.creationTimestamp?.timestamp?.time, 4_000L)
        assertFalse(messages.any { it.attributes["text"]?.contains("Build failed") == true })
    }

    private fun assertCompilationInOriginalFlow() {
        val compilationStart = messages.single { it.messageName == "compilationStarted" }
        val compilationEnd = messages.single { it.messageName == "compilationFinished" }
        val start = messages.indexOf(compilationStart)
        val end = messages.indexOf(compilationEnd)
        messages.subList(start, end + 1).forEach {
            assertEquals(it.flowId, "A")
            assertEquals(it.creationTimestamp?.timestamp?.time, 2_000L)
        }
        val flowFinished = messages.single { it.messageName == "flowFinished" }
        assertEquals(flowFinished.flowId, "A")
        assertTrue(messages.indexOf(flowFinished) > end, "The compilation error must precede flowFinished")
    }

    private fun startServer(): AutoCloseable {
        val writer = MessageWriter("") { messages.add(it) }
        val pending = PendingInvocationDiagnostics()
        val transport = mockk<GrpcServer>()
        val captured = slot<BindableService>()
        every { transport.start(capture(captured)) } returns AutoCloseable { }
        server =
            BesGrpcServer(
                writer,
                transport,
                Verbosity.Normal,
                false,
                GrpcEventHandlerChain(BuildEventHandlerChain(pending), pending),
                pending,
            )
        val closeable = server.start()
        service = captured.captured as BesGrpcServerEventStream
        return closeable
    }

    private fun startInvocation(id: String) {
        lifecycle(id, 1) { invocationAttemptStartedBuilder.attemptNumber = 1 }
        bazelEvent(id, 1, buildEvent { startedBuilder.command = "test" })
    }

    private fun failedInvocation(
        id: String,
        exitCode: Int,
    ) {
        startInvocation(id)
        bazelEvent(
            id,
            2,
            buildEvent {
                actionBuilder.apply {
                    type = "Compile"
                    success = false
                    this.exitCode = exitCode
                }
            },
        )
        bazelEvent(
            id,
            3,
            buildEvent {
                finishedBuilder.exitCodeBuilder.apply {
                    code = exitCode
                    name = if (exitCode == 39) "REMOTE_CACHE_EVICTED" else "BUILD_FAILURE"
                }
            },
        )
        lifecycle(id, 4) { invocationAttemptFinishedBuilder.invocationStatusBuilder.result = BuildStatus.Result.COMMAND_FAILED }
    }

    private fun lifecycle(
        id: String,
        seconds: Long,
        event: BuildEvent.Builder.() -> Unit,
    ) {
        service.publishLifecycleEvent(
            PublishLifecycleEventRequest.newBuilder().setBuildEvent(orderedEvent(id, seconds, event)).build(),
            mockk(relaxed = true),
        )
    }

    private fun bazelEvent(
        id: String,
        seconds: Long,
        event: BuildEventStreamProtos.BuildEvent,
    ) {
        val stream = service.publishBuildToolEventStream(mockk(relaxed = true))
        stream.onNext(
            PublishBuildToolEventStreamRequest
                .newBuilder()
                .setOrderedBuildEvent(orderedEvent(id, seconds) { bazelEvent = Any.pack(event) })
                .build(),
        )
        stream.onCompleted()
    }

    private fun orderedEvent(
        id: String,
        seconds: Long,
        event: BuildEvent.Builder.() -> Unit,
    ): OrderedBuildEvent =
        OrderedBuildEvent
            .newBuilder()
            .setStreamId(StreamId.newBuilder().setBuildId("build-$id").setInvocationId(id))
            .setSequenceNumber(seconds)
            .setEvent(BuildEvent.newBuilder().apply { eventTimeBuilder.setSeconds(seconds) }.apply(event))
            .build()
}
