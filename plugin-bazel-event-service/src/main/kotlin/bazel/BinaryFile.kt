

package bazel

import bazel.bazel.events.BazelContent
import bazel.bazel.events.BazelEvent
import bazel.events.BuildComponent
import bazel.events.StreamId
import bazel.events.Timestamp
import bazel.messages.*
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import devteam.rx.*
import java.io.File

class BinaryFile(
        private val _eventFile: File,
        private val _verbosity: Verbosity,
        private val _bazelEventConverter: Converter<BuildEventStreamProtos.BuildEvent, BazelContent>,
        private val _messageFactory: MessageFactory)
    : Observable<String> {
    override fun subscribe(observer: Observer<String>): Disposable {
        val controllerSubject = ControllerSubject(_verbosity, _messageFactory, HierarchyImpl()) {
            StreamSubject(
                _verbosity,
                _messageFactory,
                HierarchyImpl()
            )
        }
        val subscription = controllerSubject.subscribe(observer(
            onNext = { observer.onNext(it.asString()) },
            onError = { observer.onError(it) },
            onComplete = { observer.onComplete() }
        ))

        try {
            val stream = _eventFile.inputStream()
            stream.use {
                var sequenceNumber: Long = 0;
                while (stream.available() > 0) {
                    val bazelEvent = BuildEventStreamProtos.BuildEvent.parseDelimitedFrom(stream)
                    val content: BazelContent = _bazelEventConverter.convert(bazelEvent)
                    val convertedPayload = BazelEvent(
                            streamId,
                            sequenceNumber++,
                            Timestamp.zero,
                            content)

                    controllerSubject.onNext(Event("", convertedPayload))
                }
            }
        }
        catch (ex: Exception) {
            observer.onNext(_messageFactory.createErrorMessage("Cannot parse build event file \"${_eventFile.canonicalPath}\".", ex.message).asString())
        }

        return subscription
    }

    companion object {
        private val streamId = StreamId("", "", BuildComponent.Tool)
    }
}