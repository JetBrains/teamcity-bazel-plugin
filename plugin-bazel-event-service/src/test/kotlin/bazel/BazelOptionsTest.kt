package bazel

import org.testng.Assert
import org.testng.annotations.Test

class BazelOptionsTest {
    @Test
    fun testDefaultMaxMessageSize() {
        val options = BazelOptions(arrayOf())
        Assert.assertEquals(options.maxMessageSizeMb, 8, "Default max message size should be 8MB")
    }

    @Test
    fun testCustomMaxMessageSize() {
        val options = BazelOptions(arrayOf("-m", "16"))
        Assert.assertEquals(options.maxMessageSizeMb, 16, "Custom max message size should be 16MB")
    }

    @Test
    fun testMaxMessageSizeWithLongOption() {
        val options = BazelOptions(arrayOf("--max-message-size", "16"))
        Assert.assertEquals(options.maxMessageSizeMb, 16, "Custom max message size should be 16MB")
    }

    @Test
    fun testAllOptions() {
        val options =
            BazelOptions(
                arrayOf(
                    "-l",
                    "Verbose",
                    "-p",
                    "8080",
                    "-f",
                    "/tmp/event.bin",
                    "-c",
                    "/tmp/bazel.cmd",
                    "-m",
                    "16",
                ),
            )
        Assert.assertEquals(options.verbosity, Verbosity.Verbose)
        Assert.assertEquals(options.port, 8080)
        Assert.assertEquals(options.eventFile?.toString(), "/tmp/event.bin")
        Assert.assertEquals(options.bazelCommandlineFile?.path, "/tmp/bazel.cmd")
        Assert.assertEquals(options.maxMessageSizeMb, 16)
    }
}
