package bazel

import bazel.messages.MessageWriter
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.protobuf.CodedOutputStream
import org.testng.Assert.*
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class BinaryFileEventStreamTest {
    private lateinit var tempDir: Path
    private lateinit var messageWriter: MessageWriter
    private val messages = Collections.synchronizedList(mutableListOf<String>())

    @BeforeMethod
    fun setUp() {
        tempDir = Files.createTempDirectory("bep-test")
        messages.clear()
        messageWriter = MessageWriter(messagePrefix = "") { messages.add(it.toString()) }
    }

    @AfterMethod
    fun tearDown() {
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun readsValidEvents() {
        val file = tempDir.resolve("events.bin")
        Files.newOutputStream(file).use { out ->
            makeEvent(1).writeDelimitedTo(out)
            makeEvent(2).writeDelimitedTo(out)
            makeEvent(3).writeDelimitedTo(out)
        }

        val events = readEventsFromFile(file, expectedCount = 3)

        assertEquals(events.size, 3)
        assertEquals(
            events[0]
                .event.id.progress.opaqueCount,
            1,
        )
        assertEquals(
            events[1]
                .event.id.progress.opaqueCount,
            2,
        )
        assertEquals(
            events[2]
                .event.id.progress.opaqueCount,
            3,
        )
    }

    @Test
    fun skipsCorruptedEventAndContinuesReading() {
        val file = tempDir.resolve("events.bin")
        Files.newOutputStream(file).use { out ->
            makeEvent(1).writeDelimitedTo(out)
            writeUnreadableEvent(out)
            makeEvent(2).writeDelimitedTo(out)
        }

        val events = readEventsFromFile(file, expectedCount = 2)

        assertEquals(events.size, 2)
        assertEquals(
            events[0]
                .event.id.progress.opaqueCount,
            1,
        )
        assertEquals(
            events[1]
                .event.id.progress.opaqueCount,
            2,
        )
        assertTrue(
            messages.any { it.contains("Skipped unreadable") },
            "Expected warning about skipped event, got: $messages",
        )
    }

    @Test
    fun readsEventAppendedAfterTruncatedOne() {
        val file = tempDir.resolve("events.bin")

        // Serialize event2 so we can split it into two writes
        val event2Bytes = ByteArrayOutputStream()
        makeEvent(2).writeDelimitedTo(event2Bytes)
        val fullEvent2 = event2Bytes.toByteArray()
        val splitPoint = fullEvent2.size / 2

        // Write event1 + first half of event2 (truncated)
        Files.newOutputStream(file).use { out ->
            makeEvent(1).writeDelimitedTo(out)
            out.write(fullEvent2, 0, splitPoint)
        }

        val events = Collections.synchronizedList(mutableListOf<BinaryFileEventStream.Result.Event>())
        val firstLatch = CountDownLatch(1)
        val secondLatch = CountDownLatch(2)

        val stream = BinaryFileEventStream(messageWriter)
        val closeable =
            stream.create(file).start { result ->
                if (result is BinaryFileEventStream.Result.Event) {
                    events.add(result)
                    firstLatch.countDown()
                    secondLatch.countDown()
                }
            }

        try {
            assertTrue(
                firstLatch.await(5, TimeUnit.SECONDS),
                "Timed out waiting for first event",
            )
            assertEquals(events.size, 1)
            assertEquals(
                events[0]
                    .event.id.progress.opaqueCount,
                1,
            )

            // Append remaining bytes to complete event2
            Files.newOutputStream(file, StandardOpenOption.APPEND).use { out ->
                out.write(fullEvent2, splitPoint, fullEvent2.size - splitPoint)
            }

            assertTrue(
                secondLatch.await(5, TimeUnit.SECONDS),
                "Timed out waiting for second event after completing truncated write",
            )
            assertEquals(events.size, 2)
            assertEquals(
                events[1]
                    .event.id.progress.opaqueCount,
                2,
            )
        } finally {
            closeable.close()
        }
    }

    @Test
    fun waitsForPartiallyFlushedEventThenReadsItComplete() {
        val file = tempDir.resolve("events.bin")

        val event1 =
            buildEvent {
                idBuilder.progressBuilder.opaqueCount = 1
                addChildrenBuilder().progressBuilder.opaqueCount = 2
                progressBuilder
            }
        val event2 = makeEvent(3)

        val buf1 = ByteArrayOutputStream()
        event1.writeDelimitedTo(buf1)
        val fullEvent1 = buf1.toByteArray()

        // Write event1 minus last 2 bytes — simulates BufferedOutputStream
        // not having flushed the progress payload yet
        Files.newOutputStream(file).use { out ->
            out.write(fullEvent1, 0, fullEvent1.size - 2)
        }

        val events = Collections.synchronizedList(mutableListOf<BinaryFileEventStream.Result.Event>())
        val bothEvents = CountDownLatch(2)

        val stream = BinaryFileEventStream(messageWriter)
        val closeable =
            stream.create(file).start { result ->
                if (result is BinaryFileEventStream.Result.Event) {
                    events.add(result)
                    bothEvents.countDown()
                }
            }

        try {
            // Give the reader a chance to see the truncated data and NOT emit it
            Thread.sleep(500)
            assertEquals(events.size, 0, "Should not emit partially-flushed event")

            // Bazel flushes: remaining 2 bytes of event1 + full event2
            Files.newOutputStream(file, StandardOpenOption.APPEND).use { out ->
                out.write(fullEvent1, fullEvent1.size - 2, 2)
                event2.writeDelimitedTo(out)
            }

            assertTrue(
                bothEvents.await(5, TimeUnit.SECONDS),
                "Timed out waiting for events after flush completed",
            )
            assertEquals(events.size, 2)
            assertTrue(
                events[0].event.hasProgress(),
                "Event 1 should have progress payload (read complete, not truncated)",
            )
            assertEquals(
                events[1]
                    .event.id.progress.opaqueCount,
                3,
            )
        } finally {
            closeable.close()
        }
    }

    /**
     * Bazel recreates the --build_event_binary_file when it restarts an invocation, e.g. after a
     * REMOTE_CACHE_EVICTED error. The file then shrinks under the reader, which used to throw
     * IllegalArgumentException and kill the reader thread, losing every event of the final attempt.
     */
    @Test
    fun keepsReadingAfterBazelRewritesTheFileOnRetry() {
        val file = tempDir.resolve("events.bin")

        Files.write(file, framed(makeEvent(1), makeEvent(2), makeEvent(3)))

        Reader(file).use { reader ->
            reader.awaitEvents(3)

            Files.write(file, framed(makeEvent(7)), StandardOpenOption.TRUNCATE_EXISTING)

            reader.awaitTrailingEvents(7)
            reader.assertNoErrors()
            reader.assertSawRestart()
        }
    }

    /**
     * The worst case for size-based detection: the new stream is written over the old one and is
     * longer, so the file size only ever grows and the reader never observes it shrink. Only the
     * changed opening bytes of the stream reveal that the events are no longer the ones we were
     * part-way through reading.
     */
    @Test
    fun keepsReadingWhenTheRewrittenFileNeverAppearsSmaller() {
        val file = tempDir.resolve("events.bin")

        val firstAttempt = framed(makeEvent(1), makeEvent(2))
        Files.write(file, firstAttempt)

        Reader(file).use { reader ->
            reader.awaitEvents(2)

            val secondAttempt = framed(makeEvent(11), makeEvent(12), makeEvent(13))
            assertTrue(
                secondAttempt.size > firstAttempt.size,
                "The replacement stream must be longer for this test to mean anything",
            )
            overwriteInPlace(file, secondAttempt)

            reader.awaitTrailingEvents(11, 12, 13)
            reader.assertNoErrors()
            reader.assertSawRestart()
        }
    }

    /**
     * `BuildEvent` puts `id` and the announced `children` before the payload carrying the
     * invocation uuid, so two attempts of the same command share a long identical opening run of
     * bytes. Comparing a fixed-size prefix would call them the same stream; comparing the whole
     * first event does not.
     */
    @Test
    fun tellsStreamsApartWhenTheirFirstEventsShareALongPrefix() {
        val file = tempDir.resolve("events.bin")

        // Shaped as two attempts of one command are: same announced children, different uuid
        val firstAttempt = startedEvent(uuid = "11111111-1111-1111-1111-111111111111")
        val secondAttempt = startedEvent(uuid = "22222222-2222-2222-2222-222222222222")

        val sharedPrefix =
            firstAttempt
                .toByteArray()
                .zip(secondAttempt.toByteArray())
                .takeWhile { it.first == it.second }
                .size
        assertTrue(
            sharedPrefix > 64,
            "This test is only meaningful if the two events share more than 64 leading bytes, shared: $sharedPrefix",
        )

        val firstBytes = framed(firstAttempt)
        Files.write(file, firstBytes)

        Reader(file).use { reader ->
            reader.awaitEvents(1)

            // Only the uuid inside the first event differs, and the file only grows
            val secondBytes = framed(secondAttempt, makeEvent(42))
            assertTrue(secondBytes.size > firstBytes.size, "The replacement stream must be longer")
            overwriteInPlace(file, secondBytes)

            reader.awaitTrailingEvents(42)
            reader.assertNoErrors()
            reader.assertSawRestart()
        }
    }

    /**
     * A normal Bazel command can put thousands of target labels in the first event before the
     * `BuildStarted` payload. The invocation uuid can therefore be more than 128 KiB into the
     * frame, beyond the old fixed prefix used to identify the stream.
     */
    @Test
    fun tellsLargeBuildStartedEventsApartAfterInPlaceTruncation() {
        val file = tempDir.resolve("events.bin")
        val firstUuid = "11111111-1111-1111-1111-111111111111"
        val secondUuid = "22222222-2222-2222-2222-222222222222"

        val firstAttempt = startedEvent(uuid = firstUuid, targetCount = 10_000)
        val firstBytes = framed(firstAttempt)
        assertTrue(firstBytes.size > 128 * 1024, "The uuid must follow the old fixed prefix")
        Files.write(file, firstBytes)

        Reader(file).use { reader ->
            reader.awaitEvents(1)

            val secondBytes = framed(startedEvent(uuid = secondUuid, targetCount = 10_000), makeEvent(42))
            Files.write(file, secondBytes, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)

            reader.awaitEvents(2)
            reader.awaitTrailingEvents(42)
            assertEquals(reader.startedUuids, listOf(firstUuid, secondUuid))
            reader.assertSawRestart()
            reader.assertNoErrors()
        }
    }

    /**
     * The retry may also replace the file instead of truncating it in place. The open channel then
     * still refers to the old file, whose size never changes again, so the reader would wait
     * forever on a stream nobody writes to unless it reopens from the path.
     */
    @Test
    fun keepsReadingAfterBazelReplacesTheFileOnRetry() {
        val file = tempDir.resolve("events.bin")

        Files.write(file, framed(makeEvent(1)))

        Reader(file).use { reader ->
            reader.awaitEvents(1)

            // A new, larger file: nothing shrinks, so only the changed stream reveals the retry
            Files.delete(file)
            Files.write(file, framed(makeEvent(8), makeEvent(9)))

            reader.awaitTrailingEvents(8, 9)
            reader.assertNoErrors()
            reader.assertSawRestart()
        }
    }

    /**
     * A short retry can replace the file and finish inside a single poll interval, so the read that
     * happens after the loop has been told to stop must check for a new stream as well — otherwise
     * it drains the superseded one and the final attempt is lost entirely.
     */
    @Test
    fun readsAReplacementStreamThatArrivesJustBeforeClosing() {
        val file = tempDir.resolve("events.bin")
        Files.write(file, framed(makeEvent(1)))

        val reader = Reader(file)
        reader.awaitEvents(1)

        // Back to back, so that no poll iteration fits in between
        Files.delete(file)
        Files.write(file, framed(makeEvent(21), makeEvent(22)))
        reader.close()

        assertEquals(
            reader.opaqueCounts.takeLast(2),
            listOf(21, 22),
            "The replacement stream must still be read on the final drain, got: ${reader.opaqueCounts}",
        )
        reader.assertSawRestart()
        reader.assertNoErrors()
    }

    /**
     * Collects what one reader emits, for the tests that drive a stream through a restart.
     *
     * The collections are synchronized, which guards single operations but not iteration, and the
     * reader thread keeps appending while assertions run — so every traversal here locks.
     */
    private inner class Reader(
        file: Path,
    ) : AutoCloseable {
        private val events = Collections.synchronizedList(mutableListOf<BinaryFileEventStream.Result.Event>())
        private val errors = Collections.synchronizedList(mutableListOf<Throwable>())
        private val restarts = AtomicInteger()
        private val arrived = Semaphore(0)

        private val closeable =
            BinaryFileEventStream(messageWriter).create(file).start { result ->
                when (result) {
                    is BinaryFileEventStream.Result.Event -> {
                        events.add(result)
                        arrived.release()
                    }
                    is BinaryFileEventStream.Result.Error -> errors.add(result.throwable)
                    is BinaryFileEventStream.Result.StreamRestarted -> restarts.incrementAndGet()
                }
            }

        val opaqueCounts: List<Int>
            get() = synchronized(events) { events.map { it.event.id.progress.opaqueCount } }

        val startedUuids: List<String>
            get() =
                synchronized(events) {
                    events.mapNotNull { if (it.event.hasStarted()) it.event.started.uuid else null }
                }

        fun awaitEvents(count: Int) =
            assertTrue(
                arrived.tryAcquire(count, TIMEOUT_SECONDS, TimeUnit.SECONDS),
                "Timed out waiting for $count more event(s), got: $opaqueCounts",
            )

        /** Tolerates whatever a read of a half-overwritten stream may have left before [expected]. */
        fun awaitTrailingEvents(vararg expected: Int) {
            val wanted = expected.toList()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS)
            while (System.nanoTime() < deadline) {
                if (opaqueCounts.takeLast(wanted.size) == wanted) return
                Thread.sleep(50)
            }
            fail("Timed out waiting for events ending in $wanted, got: $opaqueCounts")
        }

        fun assertSawRestart() = assertTrue(restarts.get() > 0, "The new stream must be signalled as a restart")

        fun assertNoErrors() {
            val reported = synchronized(errors) { errors.toList() }
            assertTrue(reported.isEmpty(), "Reader must not fail, got: $reported")
        }

        override fun close() = closeable.close()
    }

    private fun framed(vararg events: BuildEventStreamProtos.BuildEvent): ByteArray =
        ByteArrayOutputStream().also { out -> events.forEach { it.writeDelimitedTo(out) } }.toByteArray()

    /** Writes over the file from offset 0 without truncating, so its size never shrinks. */
    private fun overwriteInPlace(
        file: Path,
        bytes: ByteArray,
    ) = Files.newByteChannel(file, StandardOpenOption.WRITE).use { it.write(ByteBuffer.wrap(bytes)) }

    /** A first BEP event as Bazel writes it: announced children first, then the payload with the uuid. */
    private fun startedEvent(
        uuid: String,
        targetCount: Int = 1,
    ): BuildEventStreamProtos.BuildEvent =
        buildEvent {
            idBuilder.startedBuilder
            addChildrenBuilder().unstructuredCommandLineBuilder
            addChildrenBuilder().structuredCommandLineBuilder.commandLineLabel = "original"
            addChildrenBuilder().structuredCommandLineBuilder.commandLineLabel = "canonical"
            addChildrenBuilder().optionsParsedBuilder
            addChildrenBuilder().workspaceStatusBuilder
            addChildrenBuilder().patternBuilder.apply {
                repeat(targetCount) { addPattern("//plugins/bazel/integrationTests/e2e:target_$it") }
            }
            addChildrenBuilder().buildFinishedBuilder
            startedBuilder.apply {
                this.uuid = uuid
                startTimeBuilder.seconds = 1_785_669_580
                buildToolVersion = "9.1.0"
                command = "test"
            }
        }

    private fun makeEvent(opaqueCount: Int): BuildEventStreamProtos.BuildEvent =
        buildEvent {
            idBuilder.progressBuilder.opaqueCount = opaqueCount
        }

    /** Writes a valid size-prefixed message whose body is garbage (all 0xFF = malformed varint tag). */
    private fun writeUnreadableEvent(out: OutputStream) {
        val body = ByteArray(8) { 0xFF.toByte() }
        val sizePrefix = ByteArrayOutputStream()
        CodedOutputStream.newInstance(sizePrefix).apply {
            writeUInt32NoTag(body.size)
            flush()
        }
        out.write(sizePrefix.toByteArray())
        out.write(body)
    }

    private companion object {
        const val TIMEOUT_SECONDS = 5L
    }

    private fun readEventsFromFile(
        file: Path,
        expectedCount: Int,
        timeoutMs: Long = 5000,
    ): List<BinaryFileEventStream.Result.Event> {
        val stream = BinaryFileEventStream(messageWriter)
        val events = Collections.synchronizedList(mutableListOf<BinaryFileEventStream.Result.Event>())
        val latch = CountDownLatch(expectedCount)

        val closeable =
            stream.create(file).start { result ->
                if (result is BinaryFileEventStream.Result.Event) {
                    events.add(result)
                    latch.countDown()
                }
            }

        try {
            assertTrue(
                latch.await(timeoutMs, TimeUnit.MILLISECONDS),
                "Timed out waiting for $expectedCount events, got ${events.size}",
            )
        } finally {
            closeable.close()
        }

        return events
    }
}
