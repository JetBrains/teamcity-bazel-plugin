package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.file.File
import bazel.file.FileConverter
import bazel.file.FileSystemService
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageWriter
import bazel.messages.TestStatusConverter
import bazel.messages.apply
import bazel.messages.toColor
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.Duration

class TestResultHandler(
    private val _fileSystemService: FileSystemService,
) : BuildEventHandler {
    private val fileConverter = FileConverter()
    private val testStatusConverter = TestStatusConverter()

    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasTestResult()) {
            return false
        }

        val event = ctx.event.testResult
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            val id = ctx.event.id
            val status = testStatusConverter.convert(event.status)
            val testAttemptDurationMillis =
                event.testAttemptDuration
                    .let { Duration.ofSeconds(it.seconds, it.nanos.toLong()) }
                    .toMillis()

            ctx.writer.message(
                buildString {
                    append("${id.testResult.label} test:")
                    append(" $status ".apply(status.toColor()))

                    if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                        append(", details: \"${event.statusDetails}\"".apply(Color.Details))
                        append(
                            (
                                ", attempt: ${id.testResult.attempt}" +
                                    ", runs: ${id.testResult.run}" +
                                    ", shard: ${id.testResult.shard}" +
                                    ", duration: $testAttemptDurationMillis(ms)" +
                                    ", cached locally: ${event.cachedLocally}"
                            ).apply(Color.Details),
                        )
                    }
                },
            )
        }

        val hasNextAttempt = ctx.event.childrenList.isNotEmpty()
        for (test in event.testActionOutputList.map { fileConverter.convert(it) }) {
            if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                ctx.writer.message("$test".apply(Color.Items))
            }

            val content = readFileLines(test, ctx.verbosity, ctx.writer)
            if (test.name.length < 3) {
                continue
            }

            when (test.name.lowercase().takeLast(3)) {
                "xml" -> {
                    // check that it is last attempt
                    if (!hasNextAttempt) {
                        // import test results
                        val testTempFile = _fileSystemService.generateTempFile("tmp", test.name)
                        _fileSystemService.write(testTempFile) { stream ->
                            OutputStreamWriter(stream).use { writer ->
                                for (line in content) {
                                    writer.write(line)
                                }
                            }
                        }
                        ctx.writer.importJUnitReport(testTempFile.absolutePath)
                    }
                }

                "log" ->
                    // check that it is last attempt
                    if (!hasNextAttempt) {
                        for (line in content) {
                            // tc:parseServiceMessagesInside is included in the message
                            ctx.writer.message(line)
                        }
                    }
            }
        }
        return true
    }

    private fun readFileLines(
        file: File,
        verbosity: Verbosity,
        writer: MessageWriter,
    ): List<String> {
        try {
            val content = InputStreamReader(file.createStream()).use { it.readLines() }
            if (verbosity.atLeast(Verbosity.Diagnostic)) {
                traceFile(file, content, writer)
            }
            return content
        } catch (ex: Exception) {
            if (verbosity.atLeast(Verbosity.Diagnostic)) {
                writer.error("Cannot read from ${file.name}.", ex.toString())
            }
            return emptyList()
        }
    }

    private fun traceFile(
        file: File,
        content: List<String>,
        writer: MessageWriter,
    ) {
        writer.trace("File \"$file\":")
        for (line in content) {
            writer.trace("$\t$line")
        }
    }
}
