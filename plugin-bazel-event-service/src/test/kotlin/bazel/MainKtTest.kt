package bazel

import bazel.messages.Message
import bazel.messages.MessageFactory
import io.mockk.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.concurrent.atomic.AtomicBoolean

class MainKtTest {
    private val exitCode = slot<Int>()

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        mockkStatic(::exit)
        every { exit(capture(exitCode)) } throws ExitException

        mockkConstructor(BazelRunner::class)
        every { anyConstructed<BazelRunner>().args } returns sequenceOf("foo", "bar")
        every { anyConstructed<BazelRunner>().run() } returns BazelRunner.Result(57, emptyList())

        mockkConstructor(MessageFactory::class)
        every { anyConstructed<MessageFactory>().createErrorMessage(any()) } returns
            Message(
                "caught ExitException",
                "Normal",
            )
    }

    @Test
    fun shouldSubscribeToBuildEventBinaryFile() {
        mockkConstructor(BinaryFile::class)
        val disposed = AtomicBoolean(false)

        every { anyConstructed<BinaryFile>().read() } answers {
            AutoCloseable { disposed.set(true) }
        }
        preventExit { main(arrayOf("-f=/fake", "-c=/fake")) }
        Assert.assertEquals(exitCode.captured, 57)

        Assert.assertTrue(disposed.get())

        verify(exactly = 1) { anyConstructed<BazelRunner>().run() }
        verify(exactly = 1) { anyConstructed<BinaryFile>().read() }
    }

    @Test
    fun shouldSubscribeToBuildEventServer() {
        mockkConstructor(GrpcServer::class)
        val disposed = AtomicBoolean(false)

        mockkConstructor(BesGrpcServer::class)
        every { anyConstructed<BesGrpcServer>().start() } answers {
            AutoCloseable { disposed.set(true) }
        }

        preventExit { main(arrayOf("-c=/fake")) }
        // Cannot verify the exit code,
        // because the SystemExitException is caught inside main.kt,
        // also setting the exit code to 1

        Assert.assertTrue(disposed.get())

        verify(exactly = 1) { anyConstructed<BazelRunner>().run() }
        verify(exactly = 1) { anyConstructed<BesGrpcServer>().start() }
    }

    object ExitException : RuntimeException()

    private fun preventExit(block: () -> Any) {
        try {
            block()
        } catch (e: ExitException) {
        }
    }
}
