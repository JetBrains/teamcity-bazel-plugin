package bazel

import bazel.messages.*
import bazel.messages.handlers.RootBazelEventHandler
import devteam.rx.*
import java.nio.file.Path

class BinaryFile(
    private val _eventFile: Path,
    private val _verbosity: Verbosity,
    private val _messageFactory: MessageFactory,
    private val _hierarchy: Hierarchy,
    private val _binaryFileStream: BinaryFileStream,
    private val _rootBazelEventHandler: RootBazelEventHandler,
) : Observable<String> {
    override fun subscribe(observer: Observer<String>): Disposable =
        _binaryFileStream.create(_eventFile).subscribe(
            observer(
                onNext = {
                    val ctx =
                        BazelEventHandlerContext(
                            _messageFactory,
                            _hierarchy,
                            _verbosity,
                            it.sequenceNumber,
                            bazelEvent = it.event,
                        ) { serviceMessage ->
                            observer.onNext(serviceMessage.toString())
                        }
                    val processed = _rootBazelEventHandler.handle(ctx)
                    if (processed) {
                        if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                            ctx.onNext(_messageFactory.createTraceMessage(ctx.bazelEvent.toString()))
                        }
                    }
                },
                onError = { observer.onError(it) },
                onComplete = { observer.onComplete() },
            ),
        )
}
