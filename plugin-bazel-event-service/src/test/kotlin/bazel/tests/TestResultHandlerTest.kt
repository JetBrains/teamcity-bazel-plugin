package bazel.tests

import bazel.Verbosity
import bazel.file.FileSystemService
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.build.TestResultHandler
import bazel.messages.Hierarchy
import bazel.messages.MessageFactory
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.protobuf.ByteString
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.messages.serviceMessages.Message
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Suppress("UNCHECKED_CAST")
class TestResultHandlerTest {
    private val serviceMessages = mutableListOf<ServiceMessage>()

    @MockK
    private lateinit var messageFactory: MessageFactory

    @MockK
    private lateinit var hierarchy: Hierarchy

    @MockK
    private lateinit var fileSystemService: FileSystemService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        serviceMessages.clear()
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
        Assert.assertTrue(serviceMessages.containsAll(listOf(message1, message2)))
        Assert.assertFalse(message1.tags.contains("tc:parseServiceMessagesInside"))
        Assert.assertTrue(message2.tags.contains("tc:parseServiceMessagesInside"))
    }

    private fun createContext(
        event: BuildEventStreamProtos.BuildEvent,
        verbosity: Verbosity,
    ) = BuildEventHandlerContext(
        verbosity,
        sequenceNumber = 42,
        messageFactory = messageFactory,
        hierarchy = hierarchy,
        event = event,
    ) {
        serviceMessages.add(it)
    }
}
