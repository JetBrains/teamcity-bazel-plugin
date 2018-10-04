package bazel

import bazel.messages.Color
import bazel.messages.apply
import devteam.rx.Disposable
import devteam.rx.use
import java.io.BufferedReader
import java.io.File
import kotlin.coroutines.experimental.buildSequence

class BazelRunner(
        private val _verbosity: Verbosity,
        bazelCommandlineFile: File,
        besPort: Int) {

    val args: List<String> = buildSequence {
        var hasBesBackendArg = false
        val besBackendArgVal = "${besBackendArg}localhost:$besPort"
        for (arg in bazelCommandlineFile.readLines()) {
            // remove existing bes_backend arg
            if (arg.startsWith(besBackendArg, true)) {
                continue
            }

            if (arg.trim() == "--") {
                yield(besBackendArgVal)
                hasBesBackendArg = true
            }

            yield(arg)
        }

        if (!hasBesBackendArg) {
            yield(besBackendArgVal)
        }
    }.toList()

    val workingDirectory: File = File(".").absoluteFile

    fun run(): Int {
        val process = ProcessBuilder(args)
                .directory(workingDirectory)
                .start()

        ActiveReader(process.inputStream.bufferedReader()) { line ->
            if (_verbosity.atLeast(Verbosity.Diagnostic)) System.out.println("> ".apply(Color.Trace) + line)
        }.use {
            ActiveReader(process.errorStream.bufferedReader()) { line ->
                if (_verbosity.atLeast(Verbosity.Diagnostic)) System.out.println("> ".apply(Color.Trace) + line)
            }.use { }
        }

        process.waitFor()
        return process.exitValue()
    }

    companion object {
        private const val besBackendArg = "--bes_backend="
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
}