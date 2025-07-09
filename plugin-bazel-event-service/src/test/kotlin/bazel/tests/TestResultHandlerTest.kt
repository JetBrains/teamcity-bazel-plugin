package bazel.tests

import bazel.Verbosity
import bazel.buildEvent
import bazel.executionInfo
import bazel.file
import bazel.file.FileSystemService
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.build.TestResultHandler
import bazel.messages.MessageWriter
import bazel.testResult
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.protobuf.ByteString
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import kotlin.io.path.Path

class TestResultHandlerTest {
    @MockK
    private lateinit var fileSystemService: FileSystemService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @DataProvider
    fun verbosityLevels(): Array<Array<Verbosity>> =
        Verbosity.entries
            .map { arrayOf(it) }
            .toTypedArray()

    @Test(dataProvider = "verbosityLevels")
    fun `should send content of test log file via service messages`(verbosity: Verbosity) {
        // arrange
        val handler = TestResultHandler(fileSystemService)
        val bazelEvent =
            buildEvent {
                testResult =
                    testResult {
                        addTestActionOutput(
                            file {
                                name = "test.log"
                                contents = ByteString.copyFromUtf8("line 1\n##teamcity[line 2]")
                            },
                        )
                    }
            }

        val serviceMessages = mutableListOf<ServiceMessage>()
        val ctx = createContext(bazelEvent, verbosity, serviceMessages)

        // act
        handler.handle(ctx)

        // assert
        serviceMessages.let {
            val content = it.joinToString("\n") { it.toString() }
            Assert.assertTrue(it.all { m -> m.tags.contains("tc:parseServiceMessagesInside") }, content)
            Assert.assertTrue(it.any { m -> m.attributes["text"] == "line 1" }, content)
            Assert.assertTrue(it.any { m -> m.attributes["text"] == "##teamcity[line 2]" }, content)
        }
    }

    @DataProvider
    fun booleans() = listOf(true, false).map { arrayOf(it) }.toTypedArray()

    @Test(dataProvider = "booleans")
    fun `should skip not existing test report when remote cache is enabled`(isCachedRemotely: Boolean) {
        // arrange
        val reportPath =
            Path(
                System.getProperty("java.io.tmpdir"),
                "not-existing-report",
            )
        val handler = TestResultHandler(fileSystemService)
        val bazelEvent =
            buildEvent {
                testResult =
                    testResult {
                        addTestActionOutput(
                            file {
                                name = "test.log"
                                uri = reportPath.toUri().toString()
                            },
                        )
                        executionInfo =
                            executionInfo {
                                setCachedRemotely(isCachedRemotely)
                            }
                    }
            }

        val serviceMessages = mutableListOf<ServiceMessage>()
        val ctx = createContext(bazelEvent, Verbosity.Normal, serviceMessages)

        // act
        handler.handle(ctx)

        if (isCachedRemotely) {
            Assert.assertEquals(serviceMessages.count(), 0)
        } else {
            // should log error
            Assert.assertEquals(serviceMessages.count(), 1)
            Assert.assertEquals(serviceMessages[0].attributes["status"], "ERROR")
        }
    }

    private fun createContext(
        event: BuildEventStreamProtos.BuildEvent,
        verbosity: Verbosity,
        serviceMessages: MutableList<ServiceMessage>,
    ) = BuildEventHandlerContext(
        verbosity,
        event = event,
        MessageWriter("") { serviceMessages.add(it) },
    )
}
