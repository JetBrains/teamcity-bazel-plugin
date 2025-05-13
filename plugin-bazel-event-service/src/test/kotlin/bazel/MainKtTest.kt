package bazel

import bazel.messages.Message
import bazel.messages.MessageFactoryImpl
import devteam.rx.Observer
import devteam.rx.disposableOf
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

        mockkConstructor(MessageFactoryImpl::class)
        every { anyConstructed<MessageFactoryImpl>().createErrorMessage(any()) } returns Message("caught ExitException", "Normal")
    }

    @Test
    fun shouldSubscribeToBuildEventBinaryFile() {
        mockkConstructor(BinaryFile::class)
        val subscriberSlot = slot<Observer<String>>()
        val disposed = AtomicBoolean(false)
        every { anyConstructed<BinaryFile>().subscribe(capture(subscriberSlot)) } answers {
            subscriberSlot.captured.onNext("next 1")
            subscriberSlot.captured.onNext("next 2")
            subscriberSlot.captured.onComplete()
            disposableOf {
                disposed.set(true)
            }
        }

        preventExit { main(arrayOf("-f=/fake", "-c=/fake")) }
        Assert.assertEquals(exitCode.captured, 57)

        Assert.assertTrue(disposed.get())

        verify(exactly = 1) { anyConstructed<BazelRunner>().run() }
        verify(exactly = 1) { anyConstructed<BinaryFile>().subscribe(any()) }
    }

    @Test
    fun shouldSubscribeToBuildEventServer() {
        mockkConstructor(GRpcServer::class)
        every { anyConstructed<GRpcServer>().port } returns 1234
        val disposed = AtomicBoolean(false)

        mockkConstructor(BesServer::class)
        val subscriberSlot = slot<Observer<String>>()
        every { anyConstructed<BesServer>().subscribe(capture(subscriberSlot)) } answers {
            subscriberSlot.captured.onNext("next 1")
            subscriberSlot.captured.onNext("next 2")
            subscriberSlot.captured.onComplete()
            disposableOf {
                disposed.set(true)
            }
        }

        preventExit { main(arrayOf("-c=/fake")) }
        // Cannot verify the exit code,
        // because the SystemExitException is caught inside main.kt,
        // also setting the exit code to 1

        Assert.assertTrue(disposed.get())

        verify(exactly = 1) { anyConstructed<BazelRunner>().run() }
        verify(exactly = 1) { anyConstructed<BesServer>().subscribe(any()) }
    }

    object ExitException : RuntimeException()

    private fun preventExit(block: () -> Any) {
        try {
            block()
        } catch (e: ExitException) {
        }
    }
}
