package bazel

import bazel.messages.Color
import bazel.messages.apply
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

        val reader = process.errorStream.bufferedReader()
        do {
            val line = reader.readLine()
            if (!line.isNullOrBlank() && _verbosity.atLeast(Verbosity.Diagnostic)) {
                System.out.println("> ".apply(Color.Trace) + line)
            }
        } while (line != null)

        process.waitFor()
        return process.exitValue()
    }
}