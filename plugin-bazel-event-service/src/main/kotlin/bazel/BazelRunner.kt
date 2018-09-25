package bazel

import bazel.messages.Color
import bazel.messages.apply
import devteam.rx.Disposable
import devteam.rx.use
import java.io.BufferedReader
import java.io.File

class BazelRunner(
        private val _verbosity: Verbosity,
        bazelCommandlineFile: File,
        private val _besPort: Int) {

    val args: List<String> = bazelCommandlineFile.readLines()

    val workingDirectory: File = File(".").absoluteFile

    fun run(): Int {
        val process = ProcessBuilder(args + listOf("--bes_backend=localhost:$_besPort"))
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