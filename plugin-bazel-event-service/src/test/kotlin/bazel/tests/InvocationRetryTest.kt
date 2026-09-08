package bazel.tests

import bazel.Verbosity
import bazel.buildEvent
import bazel.handlers.BuildEventHandlerChain
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.MessageWriter
import bazel.messages.PendingInvocationDiagnostics
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import org.testng.Assert.assertEquals
import org.testng.Assert.assertFalse
import org.testng.Assert.assertTrue
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

/**
 * Bazel restarts an invocation by itself when it hits a transient remote cache error
 * (`REMOTE_CACHE_EVICTED`, exit code 39) — `--experimental_remote_cache_eviction_retries` defaults
 * to 5. Failures of an attempt that Bazel discarded must not be turned into TeamCity build problems.
 */
class InvocationRetryTest {
    private val messages = mutableListOf<String>()
    private lateinit var writer: MessageWriter
    private lateinit var pendingDiagnostics: PendingInvocationDiagnostics
    private lateinit var chain: BuildEventHandlerChain

    @BeforeMethod
    fun setUp() {
        messages.clear()
        writer = MessageWriter(messagePrefix = "") { messages.add(it.toString()) }
        pendingDiagnostics = PendingInvocationDiagnostics()
        chain = BuildEventHandlerChain(pendingDiagnostics)
    }

    @Test
    fun doesNotReportFailuresOfAnAttemptSupersededByARetry() {
        handle(buildStarted())
        handle(failedAction())
        handle(buildFinished(REMOTE_CACHE_EVICTED))

        // Bazel retries: a second BuildStarted supersedes everything the first attempt reported
        handle(buildStarted())
        handle(buildFinished(0))
        pendingDiagnostics.flush(writer)

        assertFalse(
            messages.any { it.contains("compilationStarted") },
            "A superseded attempt must not produce a compilation error, got: $messages",
        )
        assertTrue(
            messages.any { it.contains("Bazel restarted the invocation") },
            "Expected a warning about the superseded attempt, got: $messages",
        )
        assertFalse(
            messages.any { it.contains("Build failed") },
            "The superseded attempt's exit code must not be reported either, got: $messages",
        )
    }

    @Test
    fun reportsTheExitCodeOfTheLastAttemptOnly() {
        handle(buildStarted())
        handle(buildFinished(REMOTE_CACHE_EVICTED))

        handle(buildStarted())
        handle(buildFinished(1))
        pendingDiagnostics.flush(writer)

        assertFalse(
            messages.any { it.contains("REMOTE_CACHE_EVICTED") },
            "The superseded exit code must not be reported, got: $messages",
        )
        assertEquals(
            messages.count { it.contains("Build failed") },
            1,
            "Exactly the last attempt's exit code should be reported, got: $messages",
        )
    }

    @Test
    fun reportsFailuresOfTheLastAttempt() {
        handle(buildStarted())
        handle(failedAction())
        handle(buildFinished(REMOTE_CACHE_EVICTED))

        handle(buildStarted())
        handle(failedAction())
        handle(buildFinished(1))
        pendingDiagnostics.flush(writer)

        assertEquals(
            messages.count { it.contains("compilationStarted") },
            1,
            "Only the last attempt's failure should be reported, got: $messages",
        )
        assertTrue(
            messages.any { it.contains("Action") && it.contains("failed to execute") },
            "Expected the failed action to be reported, got: $messages",
        )
    }

    /**
     * `--experimental_remote_cache_eviction_retries` is finite. Once the stream ends on an attempt
     * that failed the way Bazel retries, the retries were exhausted and the failure is the build's.
     */
    @Test
    fun reportsTheLastAttemptWhenRetriesAreExhausted() {
        handle(buildStarted())
        handle(failedAction())
        handle(buildFinished(REMOTE_CACHE_EVICTED))

        handle(buildStarted())
        handle(failedAction())
        handle(buildFinished(REMOTE_CACHE_EVICTED))
        pendingDiagnostics.flush(writer)

        assertEquals(
            messages.count { it.contains("compilationStarted") },
            1,
            "Only the last attempt's failure should be reported, got: $messages",
        )
        val failures = messages.filter { it.contains("Build failed") }
        assertEquals(
            failures.size,
            1,
            "The exhausted attempt's exit code should be reported once, got: $messages",
        )
        assertTrue(
            failures.single().contains("REMOTE_CACHE_EVICTED"),
            "The reported exit code should be the one the retries could not get past, got: $failures",
        )
        assertEquals(
            messages.count { it.contains("Bazel restarted the invocation") },
            1,
            "Only the first of the two attempts was superseded, got: $messages",
        )
    }

    /**
     * A BES server can serve several builds before it is closed, so a following BuildStarted is
     * only a retry when the previous invocation ended the way Bazel retries. Anything else is a
     * separate run whose failures still have to be reported.
     */
    @Test
    fun reportsTheFailuresOfAnEarlierUnrelatedBuild() {
        handle(buildStarted())
        handle(failedAction())
        handle(buildFinished(1))

        // A second, independent build starts on the same server
        handle(buildStarted())
        handle(buildFinished(0))
        pendingDiagnostics.flush(writer)

        assertEquals(
            messages.count { it.contains("compilationStarted") },
            1,
            "The earlier build's failure must still be reported, got: $messages",
        )
        assertFalse(
            messages.any { it.contains("Bazel restarted the invocation") },
            "A separate build is not a retry, got: $messages",
        )
    }

    @Test
    fun reportsFailuresWhenThereIsNoRetry() {
        handle(buildStarted())
        handle(failedAction())
        handle(buildFinished(1))
        pendingDiagnostics.flush(writer)

        assertEquals(
            messages.count { it.contains("compilationStarted") },
            1,
            "A failure of the only attempt must be reported, got: $messages",
        )
    }

    /**
     * In binary-file mode the superseded attempt's `BuildFinished` can be overwritten before the
     * reader reaches it, so the restart is established from the replaced stream instead.
     */
    @Test
    fun dropsAnAttemptWhoseStreamWasReplacedBeforeItsExitCodeWasRead() {
        handle(buildStarted())
        handle(failedAction())

        pendingDiagnostics.discardSuperseded(writer)

        handle(buildStarted())
        handle(buildFinished(0))
        pendingDiagnostics.flush(writer)

        assertFalse(
            messages.any { it.contains("compilationStarted") },
            "A superseded attempt must not produce a compilation error, got: $messages",
        )
        assertEquals(
            messages.count { it.contains("Bazel restarted the invocation") },
            1,
            "Expected one warning about the superseded attempt, got: $messages",
        )
        assertFalse(
            messages.any { it.contains("Build failed") },
            "The attempt that replaced it succeeded, got: $messages",
        )
    }

    private fun handle(event: BuildEventStreamProtos.BuildEvent) = chain.handle(BuildEventHandlerContext(Verbosity.Normal, event, writer))

    private fun buildStarted() =
        buildEvent {
            startedBuilder.command = "test"
        }

    private fun failedAction() =
        buildEvent {
            actionBuilder.apply {
                type = "TestRunner"
                success = false
                exitCode = REMOTE_CACHE_EVICTED
            }
        }

    private fun buildFinished(code: Int) =
        buildEvent {
            finishedBuilder.exitCodeBuilder.apply {
                this.code = code
                name =
                    when (code) {
                        0 -> "SUCCESS"
                        REMOTE_CACHE_EVICTED -> "REMOTE_CACHE_EVICTED"
                        else -> "BUILD_FAILURE"
                    }
            }
        }

    private companion object {
        const val REMOTE_CACHE_EVICTED = 39
    }
}
