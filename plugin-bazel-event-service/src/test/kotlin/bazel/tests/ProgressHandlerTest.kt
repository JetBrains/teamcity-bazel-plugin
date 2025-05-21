package bazel.tests

import bazel.Verbosity
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Hierarchy
import bazel.messages.MessageFactory
import bazel.messages.handlers.EventHandler
import bazel.messages.handlers.ProgressHandler
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import devteam.rx.*
import devteam.rx.observer
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.messages.serviceMessages.Message
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class ProgressHandlerTest {
    private lateinit var subject: Subject<ServiceMessage>
    private lateinit var actualNotifications: MutableList<Notification<ServiceMessage>>
    private lateinit var subscription: Disposable

    @MockK
    private lateinit var iterator: Iterator<EventHandler>

    @MockK
    private lateinit var messageFactory: MessageFactory

    @MockK
    private lateinit var hierarchy: Hierarchy

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        subject = subjectOf()
        actualNotifications = mutableListOf()
        subscription =
            subject
                .materialize()
                .subscribe(
                    observer(
                        onNext = { it: Notification<ServiceMessage> -> actualNotifications.add(it) },
                        onError = { },
                        onComplete = {},
                    ),
                )
    }

    @AfterMethod
    fun tearDown() {
        subscription.dispose()
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
        BazelEventHandlerContext(subject, iterator, createEvent(event), messageFactory, hierarchy, Verbosity.Normal, event)
}
