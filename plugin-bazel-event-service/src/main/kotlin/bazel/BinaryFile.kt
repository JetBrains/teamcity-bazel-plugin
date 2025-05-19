package bazel

import bazel.messages.*
import devteam.rx.*
import java.nio.file.Path

class BinaryFile(
    private val _eventFile: Path,
    private val _verbosity: Verbosity,
    private val _messageFactory: MessageFactory,
    private val _binaryFileStream: BinaryFileStream,
) : Observable<String> {
    override fun subscribe(observer: Observer<String>): Disposable {
        val controllerSubject =
            ControllerSubject(_verbosity, _messageFactory, HierarchyImpl()) {
                StreamSubject(
                    _verbosity,
                    _messageFactory,
                    HierarchyImpl(),
                )
            }
        val subscription =
            disposableOf(
                controllerSubject.subscribe(
                    observer(
                        onNext = { observer.onNext(it.asString()) },
                        onError = { observer.onError(it) },
                        onComplete = { observer.onComplete() },
                    ),
                ),
                _binaryFileStream.create(_eventFile).subscribe(
                    observer(
                        onNext = { controllerSubject.onNext(Event("", it)) },
                        onError = { controllerSubject.onError(it) },
                        onComplete = { controllerSubject.onComplete() },
                    ),
                ),
            )

        return subscription
    }
}
