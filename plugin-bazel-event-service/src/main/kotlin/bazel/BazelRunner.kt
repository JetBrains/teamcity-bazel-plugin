package bazel

import java.io.File

class BazelRunner(
        bazelCommandlineFile: File,
        besPort: Int) {

    private var args: List<String> = bazelCommandlineFile.readLines() + listOf("--bes_backend=localhost:$besPort")

    fun run(): Int {
        val process = ProcessBuilder(args)
                .directory(File(".").absoluteFile)
                //.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                //.redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()

        process.waitFor()
        return process.exitValue()
    }
}