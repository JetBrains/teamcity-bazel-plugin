package bazel

import bazel.messages.MessageWriter
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
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

        private fun readBazelEvents(
            onEvent: (Result) -> Unit,
            channel: FileChannel,
        ) {
            val input = Channels.newInputStream(channel)

            while (true) {
                val positionBeforeRead = channel.position()
                val evt = BuildEventStreamProtos.BuildEvent.parseDelimitedFrom(input)
                if (evt == null) {
                    channel.position(positionBeforeRead)
                    return
                }

                onEvent(Result.Event(sequenceNumber++, evt))
            }
        }
    }
}
