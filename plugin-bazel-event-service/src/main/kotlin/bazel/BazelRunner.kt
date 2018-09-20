package bazel

import java.io.File

class BazelRunner(
        bazelCommandlineFile: File,
        private val _besPort: Int) {

    val args: List<String> = bazelCommandlineFile.readLines()

    val workingDirectory = File(".").absoluteFile

    fun run(): Int {
        val process = ProcessBuilder(args + listOf("--bes_backend=localhost:$_besPort"))
                .directory(workingDirectory)
                //.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                //.redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

        process.waitFor()
        return process.exitValue()
    }
}