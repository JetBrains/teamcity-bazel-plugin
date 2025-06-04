package bazel.tests

import bazel.Verbosity
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.build.ProgressHandler
import bazel.messages.TargetRegistry
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ProgressHandlerTest {
    private val serviceMessages = mutableListOf<ServiceMessage>()

    @MockK
    private lateinit var targetRegistry: TargetRegistry

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        serviceMessages.clear()
    }

    @Test
    fun shouldDecomposeEventsAndSendServiceMessagesWithHighestLogLevel() {
        // Given
        val handler = ProgressHandler()
        val events =
            """
            WARNING: the reason
            description of the problem
            third line
            """.trimIndent()

        // When
        val bazelEvent =
            BuildEventStreamProtos.BuildEvent
                .newBuilder()
                .setProgress(BuildEventStreamProtos.Progress.newBuilder().setStderr(events))
                .build()

        val ctx = createContext(bazelEvent)

        handler.handle(ctx)

        // Then
        Assert.assertEquals(serviceMessages.count(), 3)
        Assert.assertTrue(serviceMessages.all { it.attributes["status"] == "WARNING" })
    }

    private fun createContext(event: BuildEventStreamProtos.BuildEvent) =
        BuildEventHandlerContext(
            Verbosity.Normal,
            sequenceNumber = 42,
            targetRegistry = targetRegistry,
            event = event,
        ) {
            serviceMessages.add(it)
        }
}
