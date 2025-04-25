

package bazel

import bazel.messages.MessageFactory
import devteam.rx.Disposable
import devteam.rx.use
import java.io.BufferedReader
import java.io.File

class BazelRunner(
    private val _messageFactory: MessageFactory,
    private val _verbosity: Verbosity,
    private val _bazelCommandlineFile: File,
    private val _besPort: Int,
    private val _eventFile: File? = null) {

    val args: Sequence<String>
        get() = sequence {
            var hasSpecialArgs = false
            for (arg in _bazelCommandlineFile.readLines()) {
                val normalizedArg = arg.replace(" ", "").replace("\"", "").replace("'", "")

                // remove existing bes_backend arg if port != 0
                if (_besPort != 0 && normalizedArg.startsWith(besBackendArg, true)) {
                    continue
                }

                // remove existing bes_backend arg if eventFile was specified
                if (_eventFile != null && normalizedArg.startsWith(eventBinaryFileArg, true)) {
                    continue
                }

                if (arg.trim() == "--") {
                    yieldAll(specialArgs)
                    hasSpecialArgs = true
                }

                yield(arg)
            }

            if (!hasSpecialArgs) {
                yieldAll(specialArgs)
            }
        }

    private val specialArgs: Sequence<String>
        get() = sequence {
            if (_besPort != 0) {
                yield("${besBackendArg}grpc://localhost:$_besPort")
            }

            if (_eventFile != null) {
                yield("${eventBinaryFileArg}${_eventFile.absolutePath}")
            }
        }

    val workingDirectory: File = File(".").absoluteFile

    fun run(): Result {
        val process = ProcessBuilder(args.toList())
                .directory(workingDirectory)
                .start()

        val errors = mutableListOf<String>()

        ActiveReader(process.inputStream.bufferedReader()) { line ->
            if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                // this message is printed by bazel itself, we will get the same message from BES/Binary log file
                // logging it as trace to reduce noise in the build log
                println(_messageFactory.createTraceMessage(line))
            }
        }.use {
            ActiveReader(process.errorStream.bufferedReader()) { line ->
                if (line.startsWith("ERROR:") || line.startsWith("FATAL:")) {
                    errors.add(line)
                }

                if (_verbosity.atLeast(Verbosity.Diagnostic)) {
                    println(_messageFactory.createTraceMessage(line))
                }
            }.use { }
        }

        process.waitFor()
        val exitCode = process.exitValue()
        return Result(exitCode, errors)
    }

    companion object {
        private const val besBackendArg = "--bes_backend="
        private const val eventBinaryFileArg = "--build_event_binary_file="
    }

    private class ActiveReader(reader: BufferedReader, action: (line: String) -> Unit) : Disposable {
        private val _tread: Thread = object : Thread() {
            override fun run() {
                do {
                    val line = reader.readLine()
                    if (!line.isNullOrBlank()) {
                        action(line)
                    }
                } while (line != null)
            }
        }

        init {
            _tread.start()
        }

        override fun dispose() = _tread.join()
    }

    data class Result(val exitCode: Int, val errors: List<String>)
}