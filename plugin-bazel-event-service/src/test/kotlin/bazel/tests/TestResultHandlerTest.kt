package bazel.tests

import bazel.Verbosity
import bazel.file.FileSystemService
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.build.TestResultHandler
import bazel.messages.TargetRegistry
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.protobuf.ByteString
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TestResultHandlerTest {
    private val serviceMessages = mutableListOf<ServiceMessage>()

    @MockK
    private lateinit var targetRegistry: TargetRegistry

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

        handler.handle(ctx)

        // Then
        serviceMessages.let {
            val content = it.joinToString("\n") { it.toString() }
            Assert.assertTrue(it.all { m -> m.tags.contains("tc:parseServiceMessagesInside") }, content)
            Assert.assertTrue(it.any { m -> m.attributes["text"] == "line 1" }, content)
            Assert.assertTrue(it.any { m -> m.attributes["text"] == "##teamcity[line 2]" }, content)
        }
    }

    private fun createContext(
        event: BuildEventStreamProtos.BuildEvent,
        verbosity: Verbosity,
    ) = BuildEventHandlerContext(
        verbosity,
        sequenceNumber = 42,
        targetRegistry = targetRegistry,
        event = event,
    ) {
        serviceMessages.add(it)
    }
}
