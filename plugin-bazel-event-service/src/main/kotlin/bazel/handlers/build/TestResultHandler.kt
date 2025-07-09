package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.file.File
import bazel.file.FileConverter
import bazel.file.FileSystemService
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.TestStatusConverter
import bazel.messages.apply
import bazel.messages.toColor
import java.io.FileNotFoundException
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

        val tests = event.testActionOutputList.map { fileConverter.convert(it) }
        if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
            tests.forEach { test ->
                if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    ctx.writer.message("$test".apply(Color.Items))
                }
            }
        }

        val isLastAttempt = ctx.event.childrenList.isEmpty()
        if (isLastAttempt) {
            tests
                .filter { it.name.endsWith(".xml") || it.name.endsWith(".log") }
                .forEach { testResults ->
                    readTestResults(
                        ctx,
                        testResults,
                        isRemoteCacheHit = event.executionInfo.cachedRemotely,
                    )
                }
        }

        return true
    }

    private fun readTestResults(
        ctx: BuildEventHandlerContext,
        test: File,
        isRemoteCacheHit: Boolean,
    ) {
        try {
            val content = readFileLines(ctx, test)
            when {
                test.name.endsWith(".xml") -> importXmlTestResults(ctx, test.name, content)
                test.name.endsWith(".log") -> printLogTestResults(ctx, content)
            }
        } catch (ex: Exception) {
            if (isRemoteCacheHit && ex is FileNotFoundException) {
                if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    ctx.writer.message(
                        "Test result file \"${test.name}\" is missing: there was a remote cache hit. " +
                            "Outputs were skipped due to remote_download_outputs setting.",
                    )
                }
            } else {
                ctx.writer.error("Cannot read from ${test.name}.", ex.toString())
            }
        }
    }

    private fun importXmlTestResults(
        ctx: BuildEventHandlerContext,
        fileName: String,
        content: List<String>,
    ) {
        val testTempFile = _fileSystemService.generateTempFile("tmp", fileName)
        _fileSystemService.write(testTempFile) { stream ->
            OutputStreamWriter(stream).use { writer -> content.forEach { writer.write(it) } }
        }
        ctx.writer.importJUnitReport(testTempFile.absolutePath)
    }

    private fun printLogTestResults(
        ctx: BuildEventHandlerContext,
        content: List<String>,
    ) {
        // print log file as a message
        // tc:parseServiceMessagesInside will parse teamcity service messages if it has any
        content.forEach { ctx.writer.message(it) }
    }

    private fun readFileLines(
        ctx: BuildEventHandlerContext,
        file: File,
    ): List<String> {
        val content = InputStreamReader(file.createStream()).use { it.readLines() }
        if (ctx.verbosity.atLeast(Verbosity.Diagnostic)) {
            ctx.writer.trace("File \"$file\":")
            content.forEach { ctx.writer.trace("$\t$it") }
        }
        return content
    }
}
