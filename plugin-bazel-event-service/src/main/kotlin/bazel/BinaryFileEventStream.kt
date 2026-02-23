package bazel

import bazel.messages.MessageWriter
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.InvalidProtocolBufferException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.io.path.exists

class BinaryFileEventStream(
    private val messageWriter: MessageWriter,
) {
    fun create(binaryFile: Path): Listener {
        messageWriter.trace("Reading Bazel events from \"$binaryFile\"...")
        return Listener(messageWriter, binaryFile)
    }

    sealed interface Result {
        data class Event(
            val sequenceNumber: Long,
            val event: BuildEventStreamProtos.BuildEvent,
        ) : Result

        data class Error(
            val throwable: Throwable,
        ) : Result
    }

    class Listener(
        private val messageWriter: MessageWriter,
        private val binaryFile: Path,
    ) {
        private val disposed = AtomicBoolean()
        private var sequenceNumber: Long = 0

        fun start(onEvent: (Result) -> Unit): AutoCloseable {
            val thread = thread(name = "BazelEventStream") { readBazelStreamLoop(onEvent) }
            return AutoCloseable {
                if (disposed.compareAndSet(false, true)) {
                    thread.join()
                }
            }
        }

        private fun readBazelStreamLoop(onEvent: (Result) -> Unit) {
            val watch = FileSystems.getDefault().newWatchService()
            var channel: FileChannel? = null

            try {
                binaryFile.parent.register(watch, ENTRY_CREATE, ENTRY_MODIFY)

                do {
                    if (channel == null && binaryFile.exists()) {
                        messageWriter.trace("Opening \"$binaryFile\" for reading...")
                        channel = FileChannel.open(binaryFile, StandardOpenOption.READ)
                    } else if (channel != null) {
                        readBazelEvents(onEvent, channel)
                    }

                    watch.poll(200, TimeUnit.MILLISECONDS)?.let {
                        it.pollEvents()
                        it.reset()
                    }
                } while (!disposed.get())

                channel?.let { readBazelEvents(onEvent, it) }

                if (channel == null) {
                    messageWriter.error("Bazel event file was not found or is not readable.")
                } else {
                    messageWriter.trace("Bazel event stream has been completed")
                }
            } catch (ex: Exception) {
                onEvent(Result.Error(ex))
            } finally {
                runCatching { channel?.close() }
                runCatching { watch.close() }
            }
        }

        /**
         * parseDelimitedFrom on a FileChannel silently returns incomplete messages
         * at EOF (proto3 treats missing fields as defaults), permanently misaligning
         * subsequent reads. We peek at the size prefix first to verify the full
         * message is on disk before parsing.
         */
        private fun readBazelEvents(
            onEvent: (Result) -> Unit,
            channel: FileChannel,
        ) {
            while (true) {
                val positionBeforeRead = channel.position()
                val messageSize = peekMessageSize(channel, positionBeforeRead) ?: return
                val prefixSize = CodedOutputStream.computeUInt32SizeNoTag(messageSize)
                val eventEnd = positionBeforeRead + prefixSize + messageSize

                // Full message not yet on disk — wait for Bazel to flush more data
                if (eventEnd > channel.size()) {
                    channel.position(positionBeforeRead)
                    return
                }

                try {
                    val input = Channels.newInputStream(channel)
                    val evt = BuildEventStreamProtos.BuildEvent.parseDelimitedFrom(input)
                    channel.position(eventEnd)
                    if (evt != null) {
                        onEvent(Result.Event(sequenceNumber++, evt))
                    }
                } catch (ex: InvalidProtocolBufferException) {
                    messageWriter.warning(
                        "Skipped unreadable bazel event at position $positionBeforeRead: ${ex.message}",
                    )
                    channel.position(eventEnd)
                    continue
                } catch (ex: Exception) {
                    messageWriter.warning("Could not read bazel event at position $positionBeforeRead")
                    messageWriter.trace("${ex.message}\n${ex.stackTraceToString()}")
                    channel.position(positionBeforeRead)
                    return
                }
            }
        }

        private fun peekMessageSize(
            channel: FileChannel,
            position: Long,
        ): Int? {
            val available = channel.size() - position
            if (available == 0L) return null

            val headerSize = minOf(available, MAX_VARINT_SIZE.toLong()).toInt()
            val headerBuf = ByteBuffer.allocate(headerSize)
            if (channel.read(headerBuf) <= 0) return null
            headerBuf.flip()

            return try {
                val codedInput = CodedInputStream.newInstance(headerBuf.array(), 0, headerBuf.remaining())
                val size = codedInput.readRawVarint32()
                if (size < 0) null else size
            } catch (_: Exception) {
                null
            } finally {
                channel.position(position)
            }
        }

        companion object {
            private const val MAX_VARINT_SIZE = 5
        }
    }
}
