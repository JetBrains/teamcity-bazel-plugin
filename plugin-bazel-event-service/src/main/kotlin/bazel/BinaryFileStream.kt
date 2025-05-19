package bazel

import bazel.bazel.converters.BazelEventConverter
import bazel.bazel.events.BazelEvent
import bazel.events.StreamId
import bazel.events.Timestamp
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import devteam.rx.Disposable
import devteam.rx.Observable
import devteam.rx.Observer
import devteam.rx.disposableOf
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Logger
import kotlin.concurrent.thread
import kotlin.io.path.exists

class BinaryFileStream(
    private val _bazelEventConverter: BazelEventConverter,
) {
    fun create(binaryFile: Path): Listener {
        logger.info("Reading Bazel events from \"$binaryFile\"...")
        return Listener(binaryFile, _bazelEventConverter)
    }

    class Listener(
        private val binaryFile: Path,
        private val _bazelEventConverter: BazelEventConverter,
    ) : Observable<BazelEvent> {
        private val disposed = AtomicBoolean()
        private var sequenceNumber: Long = 0

        override fun subscribe(observer: Observer<BazelEvent>): Disposable {
            val thread = thread(name = "BazelEventStream") { readBazelStreamLoop(observer) }
            return disposableOf {
                if (disposed.compareAndSet(false, true)) {
                    thread.join()
                }
            }
        }

        private fun readBazelStreamLoop(observer: Observer<BazelEvent>) {
            val watch = FileSystems.getDefault().newWatchService()
            var channel: FileChannel? = null

            try {
                binaryFile.parent.register(watch, ENTRY_CREATE, ENTRY_MODIFY)

                do {
                    if (channel == null && binaryFile.exists()) {
                        logger.info("Opening \"$binaryFile\" for reading...")
                        channel = FileChannel.open(binaryFile, StandardOpenOption.READ)
                    } else if (channel != null) {
                        readBazelEvents(observer, channel)
                    }

                    watch.poll(200, TimeUnit.MILLISECONDS)?.let {
                        it.pollEvents()
                        it.reset()
                    }
                } while (!disposed.get())

                channel?.let { readBazelEvents(observer, it) }
                observer.onComplete()

                if (channel == null) {
                    logger.warning("Bazel event file was not found or is not readable.")
                } else {
                    logger.info("Bazel event stream has been completed")
                }
            } catch (ex: Exception) {
                observer.onError(ex)
            } finally {
                runCatching { channel?.close() }
                runCatching { watch.close() }
            }
        }

        private fun readBazelEvents(
            observer: Observer<BazelEvent>,
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

                observer.onNext(convertBazelEvent(evt))
            }
        }

        private fun convertBazelEvent(event: BuildEventStreamProtos.BuildEvent): BazelEvent =
            BazelEvent(
                StreamId.default,
                sequenceNumber++,
                Timestamp.zero, // timestamp is available only in OrderedBuildEvent (BES grpc mode)
                _bazelEventConverter.convert(event),
            )
    }

    companion object {
        private val logger = Logger.getLogger(BinaryFileStream::class.java.name)
    }
}
