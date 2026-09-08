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
import java.security.MessageDigest
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

        /**
         * Bazel replaced the stream in the event file, which it only does when it restarts an
         * invocation. Everything read so far belongs to the attempt that was superseded.
         */
        object StreamRestarted : Result
    }

    class Listener(
        private val messageWriter: MessageWriter,
        private val binaryFile: Path,
    ) {
        private val disposed = AtomicBoolean()
        private var sequenceNumber: Long = 0
        private var streamIdentity: ByteArray? = null

        /**
         * The first frame is stable within an invocation and contains its UUID and start time.
         * Hash the whole frame: target-pattern children can put the UUID beyond any fixed prefix.
         * A small buffer bounds memory use even for large target lists. File size alone cannot
         * detect retries, since the replacement can grow past the old EOF between polls.
         */
        private fun currentStreamIdentity(): ByteArray? =
            runCatching {
                FileChannel.open(binaryFile, StandardOpenOption.READ).use { streamIdentityOf(it) }
            }.getOrNull()

        /**
         * Returns null until the whole first frame is on disk. Record the identity through the
         * active channel, so replacing the path cannot associate the new file's identity with an
         * old, unlinked channel.
         */
        private fun streamIdentityOf(channel: FileChannel): ByteArray? {
            val length = firstEventLength(channel) ?: return null
            if (channel.size() < length) return null

            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteBuffer.allocate(STREAM_IDENTITY_BUFFER_BYTES)
            var offset = 0L
            while (offset < length) {
                buffer.clear()
                buffer.limit(minOf(buffer.capacity().toLong(), length - offset).toInt())
                val read = channel.read(buffer, offset)
                if (read <= 0) return null
                offset += read
                buffer.flip()
                digest.update(buffer)
            }
            return digest.digest()
        }

        private fun firstEventLength(channel: FileChannel): Long? = peekMessageSize(channel, 0)?.let(::framedLength)

        /**
         * Reopening rather than rewinding is what covers a replaced file: the channel would
         * otherwise stay on the inode that was unlinked and never see another byte.
         */
        private fun reopenIfRestarted(
            channel: FileChannel,
            onEvent: (Result) -> Unit,
        ): FileChannel {
            val beingRead = streamIdentity
            if (beingRead == null) {
                streamIdentity = streamIdentityOf(channel)
                return channel
            }

            val atPath = currentStreamIdentity()
            if (atPath == null || atPath.contentEquals(beingRead)) {
                return channel
            }

            messageWriter.trace("Bazel wrote a new event stream, reading it from the beginning")
            runCatching { channel.close() }
            val reopened = FileChannel.open(binaryFile, StandardOpenOption.READ)
            streamIdentity = streamIdentityOf(reopened)
            onEvent(Result.StreamRestarted)
            return reopened
        }

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

            fun pump(current: FileChannel): FileChannel = reopenIfRestarted(current, onEvent).also { readBazelEvents(onEvent, it) }

            try {
                binaryFile.parent.register(watch, ENTRY_CREATE, ENTRY_MODIFY)

                do {
                    if (channel == null && binaryFile.exists()) {
                        messageWriter.trace("Opening \"$binaryFile\" for reading...")
                        channel = FileChannel.open(binaryFile, StandardOpenOption.READ)
                    } else if (channel != null) {
                        channel = pump(channel)
                    }

                    watch.poll(200, TimeUnit.MILLISECONDS)?.let {
                        it.pollEvents()
                        it.reset()
                    }
                } while (!disposed.get())

                // Bazel may have replaced the file and exited within a single poll interval, so
                // the last read has to check for that too or it would drain the superseded stream.
                channel = channel?.let(::pump)

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
                val eventEnd = positionBeforeRead + framedLength(messageSize)

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

        /** Reads at an absolute offset, leaving the channel position for the caller to own. */
        private fun peekMessageSize(
            channel: FileChannel,
            position: Long,
        ): Int? {
            val available = channel.size() - position
            if (available <= 0L) return null

            val headerBuf = ByteBuffer.allocate(minOf(available, MAX_VARINT_SIZE.toLong()).toInt())
            if (channel.read(headerBuf, position) <= 0) return null
            headerBuf.flip()

            return try {
                val codedInput = CodedInputStream.newInstance(headerBuf.array(), 0, headerBuf.remaining())
                codedInput.readRawVarint32().takeIf { it >= 0 }
            } catch (_: Exception) {
                null
            }
        }

        /** What a message of this size occupies in the file, size prefix included. */
        private fun framedLength(messageSize: Int): Long = CodedOutputStream.computeUInt32SizeNoTag(messageSize).toLong() + messageSize

        companion object {
            private const val MAX_VARINT_SIZE = 5
            private const val STREAM_IDENTITY_BUFFER_BYTES = 8 * 1024
        }
    }
}
