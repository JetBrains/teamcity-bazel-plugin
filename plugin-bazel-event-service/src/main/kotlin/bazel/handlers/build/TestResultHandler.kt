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
            when {
                test.name.endsWith(".xml") -> importXmlTestResults(ctx, test)
                test.name.endsWith(".log") -> streamLogTestResults(ctx, test)
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
        test: File,
    ) {
        test.createStream().use { input ->
            val testTempFile = _fileSystemService.generateTempFile("tmp", test.name)
            _fileSystemService.write(testTempFile) { stream ->
                input.copyTo(stream)
            }
            ctx.writer.importJUnitReport(testTempFile.absolutePath)
        }
    }

    // A test.log holds the combined stdout/stderr of the whole test action, so it can reach tens of GB
    // (e.g. a hanging test with --test_output=streamed). Do not materialize it.
    private fun streamLogTestResults(
        ctx: BuildEventHandlerContext,
        file: File,
    ) {
        val diagnostic = ctx.verbosity.atLeast(Verbosity.Diagnostic)
        if (diagnostic) {
            ctx.writer.trace("File \"$file\":")
        }

        // tc:parseServiceMessagesInside will parse teamcity service messages if the line has any
        InputStreamReader(file.createStream()).forEachLine { line ->
            ctx.writer.message(line)
            if (diagnostic) {
                ctx.writer.trace("$\t$line")
            }
        }
    }
}
