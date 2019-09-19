package bazel.tests

import bazel.Event
import bazel.FileSystem
import bazel.Verbosity
import bazel.bazel.events.File
import bazel.bazel.events.Id
import bazel.bazel.events.TestResult
import bazel.bazel.events.TestStatus
import bazel.events.OrderedBuildEvent
import bazel.messages.Hierarchy
import bazel.messages.MessageFactory
import bazel.messages.ServiceMessageContext
import bazel.messages.handlers.EventHandler
import bazel.messages.handlers.TestResultHandler
import devteam.rx.*
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.messages.serviceMessages.Message
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.testng.Assert
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Suppress("UNCHECKED_CAST")
class TestResultHandlerTest {
    private lateinit var _subject: Subject<ServiceMessage>
    private lateinit var _actualNotifications: MutableList<Notification<ServiceMessage>>
    private lateinit var _subscription: Disposable
    @MockK private lateinit var _iterator: Iterator<EventHandler>
    @MockK private lateinit var _messageFactory: MessageFactory
    @MockK private lateinit var _hierarchy: Hierarchy
    @MockK private lateinit var _fileSystem: FileSystem

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        _subject = subjectOf<ServiceMessage>()
        _actualNotifications = mutableListOf<Notification<ServiceMessage>>()
        _subscription = _subject.materialize().subscribe { _actualNotifications.add(it) }
    }

    @AfterMethod
    fun tearDown() {
        _subscription.dispose()
    }

    @DataProvider
    fun verbosityLevels(): Array<Array<Verbosity>> =
        arrayOf(
                arrayOf(Verbosity.Quiet),
                arrayOf(Verbosity.Normal),
                arrayOf(Verbosity.Detailed),
                arrayOf(Verbosity.Verbose),
                arrayOf(Verbosity.Diagnostic))

    @Test(dataProvider = "verbosityLevels")
    fun shouldSendContentOfTestLogFileViaServiceMessages(verbosity: Verbosity) {
        // Given
        val handler = TestResultHandler(_fileSystem)

        // When
        val event: Event<OrderedBuildEvent> = createEvent(
                TestResult(
                        Id(1),
                        emptyList(),
                        "label",
                        1,
                        1,
                        1,
                        TestStatus.Passed,
                        "ok",
                        true,
                        1,
                        1,
                        listOf(File("tests.log", "file:///abc/test.log")),
                        emptyList()))

        val testLogFile = java.io.File(java.io.File.separator + "abc" + java.io.File.separator + "test.log")
        val message1 = Message("line 1", "Normal", null)
        val message2 = Message("##teamcity[line 2]", "Normal", null)
        val message = Message("message", "Normal", null)

        every { _fileSystem.exists(testLogFile) } returns true
        every { _fileSystem.readFile(testLogFile) } returns sequenceOf("line 1", "##teamcity[line 2]")
        every { _messageFactory.createMessage(any()) } returns message
        every { _messageFactory.createMessage("line 1") } returns message1
        every { _messageFactory.createMessage("##teamcity[line 2]") } returns message2

        handler.handle(createContext(event, verbosity))

        // Then
        Assert.assertTrue(_actualNotifications.containsAll(listOf(NotificationNext<ServiceMessage>(message1), NotificationNext<ServiceMessage>(message2))))
        Assert.assertFalse(message1.tags.contains("tc:parseServiceMessagesInside"))
        Assert.assertTrue(message2.tags.contains("tc:parseServiceMessagesInside"))
    }

    private fun createContext(event: Event<OrderedBuildEvent>, verbosity: Verbosity) =
            ServiceMessageContext(_subject, _iterator, event, _messageFactory, _hierarchy, verbosity)
}