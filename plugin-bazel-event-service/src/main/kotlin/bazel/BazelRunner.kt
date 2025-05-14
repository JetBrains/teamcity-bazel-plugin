

package bazel

import bazel.messages.MessageFactory
import devteam.rx.Disposable
import devteam.rx.use
import java.io.BufferedReader
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class BazelRunner(
    private val messageFactory: MessageFactory,
    private val verbosity: Verbosity,
    private val bazelCommandlineFile: File,
    private val besPort: Int,
    private val eventFile: Path? = null,
) {
    val args: Sequence<String>
        get() =
            sequence {
                var hasSpecialArgs = false
                for (arg in bazelCommandlineFile.readLines()) {
                    val normalizedArg = arg.replace(" ", "").replace("\"", "").replace("'", "")

                    // remove existing bes_backend arg if port != 0
                    if (besPort != 0 && normalizedArg.startsWith(BES_BACKEND_ARG, true)) {
                        continue
                    }

                    // remove existing bes_backend arg if eventFile was specified
                    if (eventFile != null && normalizedArg.startsWith(BINARY_FILE_ARG, true)) {
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
        get() =
            sequence {
                if (besPort != 0) {
                    yield("${BES_BACKEND_ARG}grpc://localhost:$besPort")
                }

                if (eventFile != null) {
                    yield("${BINARY_FILE_ARG}${eventFile.absolutePathString()}")
                }
            }

    val workingDirectory: File = File(".").absoluteFile

    fun run(): Result {
        val process =
            ProcessBuilder(args.toList())
                .directory(workingDirectory)
                .start()

        val errors = mutableListOf<String>()

        val stdOutReader =
            ActiveReader(process.inputStream.bufferedReader()) { line ->
                if (verbosity.atLeast(Verbosity.Diagnostic)) {
                    // this message is printed by bazel itself, we will get the same message from BES/Binary log file
                    // logging it as trace to reduce noise in the build log
                    println(messageFactory.createTraceMessage(line))
                }
            }
        val stdErrReader =
            ActiveReader(process.errorStream.bufferedReader()) { line ->
                if (line.startsWith("ERROR:") || line.startsWith("FATAL:")) {
                    errors.add(line)
                }

                if (verbosity.atLeast(Verbosity.Diagnostic)) {
                    println(messageFactory.createTraceMessage(line))
                }
            }
        stdOutReader.use { stdErrReader.use {} }

        process.waitFor()
        val exitCode = process.exitValue()
        return Result(exitCode, errors)
    }

    companion object {
        private const val BES_BACKEND_ARG = "--bes_backend="
        private const val BINARY_FILE_ARG = "--build_event_binary_file="
    }

    private class ActiveReader(
        reader: BufferedReader,
        action: (line: String) -> Unit,
    ) : Disposable {
        private val thread: Thread =
            object : Thread() {
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
            thread.start()
        }

        override fun dispose() = thread.join()
    }

    data class Result(
        val exitCode: Int,
        val errors: List<String>,
    )
}
