package bazel.tests

import bazel.Verbosity
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.build.ProgressHandler
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
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

        val ctx = createContext(bazelEvent)

        val serviceMessages = handler.handle(ctx).messages.toList()

        // Then
        Assert.assertEquals(serviceMessages.count(), 3)
        Assert.assertTrue(serviceMessages.all { it.attributes["status"] == "WARNING" })
    }

    private fun createContext(event: BuildEventStreamProtos.BuildEvent) =
        BuildEventHandlerContext(
            Verbosity.Normal,
            messagePrefix = "",
            event = event,
        )
}
