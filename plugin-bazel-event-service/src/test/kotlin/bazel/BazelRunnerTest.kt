package bazel

import bazel.messages.MessageFactoryImpl
import io.mockk.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Files

class BazelRunnerTest {

    private lateinit var tempDir: File

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        tempDir = Files.createTempDirectory(this.javaClass.name).toFile()
        tempDir.deleteOnExit()
    }

    @Test
    fun shouldProcessBazelArgs() {
        val inputArgs = listOf(
                "/fake/bazel",
                "--build_event_binary_file=/should/be/omitted",
                "bar x'\"y",
                "",
                "--",
                "baz"
        )
        val commandFile = File(tempDir, "commands")
        commandFile.writeText(inputArgs.joinToString("\n"))
        val eventFile = File(tempDir, "events")
        val fixture = BazelRunner(MessageFactoryImpl(),
                Verbosity.Normal,
                commandFile,
                0,
                eventFile
        )
        val expectedArgs = listOf(
                "/fake/bazel",
                "bar x'\"y",
                "",
                "--build_event_binary_file=${eventFile.absolutePath}",
                "--",
                "baz"
        )
        Assert.assertEquals(fixture.args.toList(), expectedArgs)
    }

}