package bazel

import bazel.messages.*
import bazel.messages.handlers.RootBazelEventHandler
import devteam.rx.*
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.nio.file.Path

class BinaryFile(
    private val _eventFile: Path,
    private val _verbosity: Verbosity,
    private val _messageFactory: MessageFactory,
    private val _hierarchy: Hierarchy,
    private val _binaryStream: BinaryFileStream,
    private val _rootBazelEventHandler: RootBazelEventHandler,
) {
    fun read() =
        _binaryStream.create(_eventFile).start(
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
                            printMessage(serviceMessage)
                        }
                    val processed = _rootBazelEventHandler.handle(ctx)
                    if (processed) {
                        if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                            printMessage(_messageFactory.createTraceMessage(ctx.bazelEvent.toString()))
                        }
                    }
                },
                onError = {
                    val error = _messageFactory.createErrorMessage("Error during binary file read", it.toString())
                    printMessage(error)
                },
                onComplete = { },
            ),
        )

    private fun printMessage(message: ServiceMessage) {
        println(message.asString())
    }
}
