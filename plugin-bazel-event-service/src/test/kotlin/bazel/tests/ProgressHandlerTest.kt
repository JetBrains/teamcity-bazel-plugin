package bazel.tests

import bazel.Event
import bazel.Verbosity
import bazel.bazel.events.*
import bazel.events.OrderedBuildEvent
import bazel.messages.Hierarchy
import bazel.messages.MessageFactory
import bazel.messages.ServiceMessageContext
import bazel.messages.handlers.EventHandler
import bazel.messages.handlers.ProgressHandler
import devteam.rx.*
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
    private lateinit var _subject: Subject<ServiceMessage>
    private lateinit var _actualNotifications: MutableList<Notification<ServiceMessage>>
    private lateinit var _subscription: Disposable

    @MockK
    private lateinit var _iterator: Iterator<EventHandler>

    @MockK
    private lateinit var _messageFactory: MessageFactory

    @MockK
    private lateinit var _hierarchy: Hierarchy

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        _subject = subjectOf()
        _actualNotifications = mutableListOf()
        _subscription = _subject.materialize().subscribe { _actualNotifications.add(it) }
    }

    @AfterMethod
    fun tearDown() {
        _subscription.dispose()
    }

    @Test
    fun shouldDecomposeEventsAndSendServiceMessagesWithHighestLogLevel() {
        // Given
        val handler = ProgressHandler()
        val events = """
            WARNING: the reason
            description of the problem
            third line
        """.trimIndent()

        // When
        val event: Event<OrderedBuildEvent> = createEvent(
            Progress(
                Id(1),
                children = emptyList(),
                stdout = "",
                stderr = events
            )
        )

        val ctx = createContext(event)
        val message = Message("message", "Warning", null)

        every { _messageFactory.createWarningMessage(any()) } returns message

        handler.handle(ctx)

        // Then
        verify(exactly = 3) { _messageFactory.createWarningMessage(any()) }
    }

    private fun createContext(event: Event<OrderedBuildEvent>) =
        ServiceMessageContext(_subject, _iterator, event, _messageFactory, _hierarchy, Verbosity.Normal)
}