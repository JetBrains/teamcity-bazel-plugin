package bazel.tests

import bazel.Verbosity
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.build.ProgressHandler
import bazel.messages.MessageWriter
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.testng.Assert
import org.testng.annotations.Test

class ProgressHandlerTest {
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

        val serviceMessages = mutableListOf<ServiceMessage>()
        val ctx = createContext(bazelEvent, serviceMessages)

        handler.handle(ctx)

        // Then
        Assert.assertEquals(serviceMessages.count(), 3)
        Assert.assertTrue(serviceMessages.all { it.attributes["status"] == "WARNING" })
    }

    private fun createContext(
        event: BuildEventStreamProtos.BuildEvent,
        serviceMessages: MutableList<ServiceMessage>,
    ): BuildEventHandlerContext =
        BuildEventHandlerContext(
            Verbosity.Normal,
            event = event,
            MessageWriter(Verbosity.Normal, 0) { serviceMessages.add(it) },
        )
}
