package bazel.tests

import bazel.FileSystemService
import bazel.Verbosity
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Hierarchy
import bazel.messages.MessageFactory
import bazel.messages.handlers.TestResultHandler
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.protobuf.ByteString
import devteam.rx.*
import devteam.rx.observer
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.messages.serviceMessages.Message
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.testng.Assert
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Suppress("UNCHECKED_CAST")
class TestResultHandlerTest {
    private lateinit var subject: Subject<ServiceMessage>
    private lateinit var actualNotifications: MutableList<Notification<ServiceMessage>>
    private lateinit var subscription: Disposable

    @MockK
    private lateinit var messageFactory: MessageFactory

    @MockK
    private lateinit var hierarchy: Hierarchy

    @MockK
    private lateinit var fileSystemService: FileSystemService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        subject = subjectOf<ServiceMessage>()
        actualNotifications = mutableListOf<Notification<ServiceMessage>>()
        subscription =
            subject
                .materialize()
                .subscribe(
                    observer(
                        onNext = { it -> actualNotifications.add(it) },
                        onError = { },
                        onComplete = {},
                    ),
                )
    }

    @AfterMethod
    fun tearDown() {
        subscription.dispose()
    }

    @DataProvider
    fun verbosityLevels(): Array<Array<Verbosity>> =
        arrayOf(
            arrayOf(Verbosity.Quiet),
            arrayOf(Verbosity.Normal),
            arrayOf(Verbosity.Detailed),
            arrayOf(Verbosity.Verbose),
        )

    @Test(dataProvider = "verbosityLevels")
    fun shouldSendContentOfTestLogFileViaServiceMessages(verbosity: Verbosity) {
        // Given
        val handler = TestResultHandler(fileSystemService)

        // When
        val bazelEvent =
            BuildEventStreamProtos.BuildEvent
                .newBuilder()
                .setTestResult(
                    BuildEventStreamProtos.TestResult
                        .newBuilder()
                        .addTestActionOutput(
                            BuildEventStreamProtos.File
                                .newBuilder()
                                .setName("test.log")
                                .setContents(ByteString.copyFromUtf8("line 1\n##teamcity[line 2]")),
                        ),
                ).build()

        val ctx = createContext(bazelEvent, verbosity)
        val message1 = Message("line 1", "Normal", null)
        val message2 = Message("##teamcity[line 2]", "Normal", null)
        val message = Message("message", "Normal", null)

        every { messageFactory.createMessage(any()) } returns message
        every { messageFactory.createMessage("line 1") } returns message1
        every { messageFactory.createMessage("##teamcity[line 2]") } returns message2

        handler.handle(ctx)

        // Then
        Assert.assertTrue(
            actualNotifications.containsAll(
                listOf(
                    NotificationNext<ServiceMessage>(message1),
                    NotificationNext<ServiceMessage>(message2),
                ),
            ),
        )
        Assert.assertFalse(message1.tags.contains("tc:parseServiceMessagesInside"))
        Assert.assertTrue(message2.tags.contains("tc:parseServiceMessagesInside"))
    }

    private fun createContext(
        event: BuildEventStreamProtos.BuildEvent,
        verbosity: Verbosity,
    ) = BazelEventHandlerContext(subject, hierarchy, event, createEvent(event), messageFactory, verbosity)
}
