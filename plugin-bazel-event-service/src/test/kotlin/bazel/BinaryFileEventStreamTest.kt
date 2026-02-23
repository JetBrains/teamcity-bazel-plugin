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
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
