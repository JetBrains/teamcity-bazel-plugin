package bazel.tests

import bazel.Verbosity
import bazel.handlers.BepEventHandlerContext
import bazel.handlers.bep.ProgressHandler
import bazel.messages.Hierarchy
import bazel.messages.MessageFactory
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.messages.serviceMessages.Message
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ProgressHandlerTest {
    private val serviceMessages = mutableListOf<ServiceMessage>()

    @MockK
    private lateinit var messageFactory: MessageFactory

    @MockK
    private lateinit var hierarchy: Hierarchy

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
        val message = Message("message", "Warning", null)

        every { messageFactory.createWarningMessage(any()) } returns message

        handler.handle(ctx)

        // Then
        verify(exactly = 3) { messageFactory.createWarningMessage(any()) }
    }

    private fun createContext(event: BuildEventStreamProtos.BuildEvent) =
        BepEventHandlerContext(
            Verbosity.Normal,
            sequenceNumber = 42,
            messageFactory = messageFactory,
            hierarchy = hierarchy,
            event = event,
        ) {
            serviceMessages.add(it)
        }
}
