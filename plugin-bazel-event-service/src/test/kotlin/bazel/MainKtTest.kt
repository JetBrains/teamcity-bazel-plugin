package bazel

import com.google.devtools.build.v1.OrderedBuildEvent
import devteam.rx.Observer
import devteam.rx.emptyDisposable
import io.mockk.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import util.NoExitSecurityManager

class MainKtTest {

    @BeforeMethod
    fun setUp() {
        System.setSecurityManager(NoExitSecurityManager())
        // This is needed to disable an annoying stack trace
        System.setProperty("log4j2.disable.jmx", "true")
        MockKAnnotations.init(this)
        clearAllMocks()

        mockkConstructor(BazelRunner::class)
        every { anyConstructed<BazelRunner>().args } returns sequenceOf("foo", "bar")
        every { anyConstructed<BazelRunner>().run() } returns
                BazelRunner.Result(57, listOf("fake error 1", "fake error 2"))
    }

    @Test
    fun shouldSubscribeToBuildEventBinaryFile() {
        mockkConstructor(BinaryFile::class)
        val subscriberSlot = slot<Observer<String>>()
        every { anyConstructed<BinaryFile>().subscribe(capture(subscriberSlot)) } answers {
            subscriberSlot.captured.onNext("next 1")
            subscriberSlot.captured.onNext("next 2")
            subscriberSlot.captured.onComplete()
            emptyDisposable()
        }

        val exitCode = captureExitCode {
            main(arrayOf("-f=/fake", "-c=/fake"))
        }
        Assert.assertEquals(exitCode, 57)

        verify(exactly = 1) { anyConstructed<BazelRunner>().run() }
        verify(exactly = 1) { anyConstructed<BinaryFile>().subscribe(any()) }
    }

    //@Test
    // Cannot run more than one test case
    // because URL.setURLStreamHandlerFactory can be run only once per JVM
    fun shouldSubscribeToBuildEventServer() {
        mockkConstructor(GRpcServer::class)
        every { anyConstructed<GRpcServer>().port } returns 1234

        mockkConstructor(BesServer::class)
        val subscriberSlot = slot<Observer<String>>()
        every { anyConstructed<BesServer<OrderedBuildEvent>>().subscribe(capture(subscriberSlot)) } answers {
            subscriberSlot.captured.onNext("next 1")
            subscriberSlot.captured.onNext("next 2")
            subscriberSlot.captured.onComplete()
            emptyDisposable()
        }

        // Cannot verify the exit code,
        // because the SystemExitException is caught inside main.kt,
        // also setting the exit code to 1
        captureExitCode {
            main(arrayOf("-c=/fake"))
        }

        verify(exactly = 1) { anyConstructed<BazelRunner>().run() }
        verify(exactly = 1) { anyConstructed<BesServer<OrderedBuildEvent>>().subscribe(any()) }
    }

    private fun captureExitCode(body: () -> Any): Int {
        try {
            body()
        } catch (e: NoExitSecurityManager.Companion.SystemExitException) {
            return e.status
        }
        throw AssertionError("Expected a System.exit() call but it did not happen")
    }

}


