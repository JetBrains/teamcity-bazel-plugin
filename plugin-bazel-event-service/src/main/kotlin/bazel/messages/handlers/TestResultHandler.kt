package bazel.messages.handlers

import bazel.FileSystemService
import bazel.Verbosity
import bazel.atLeast
import bazel.events.File
import bazel.events.FileConverter
import bazel.events.TestStatusConverter
import bazel.events.readLines
import bazel.events.toColor
import bazel.messages.BazelEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import java.io.OutputStreamWriter
import java.time.Duration

class TestResultHandler(
    private val _fileSystemService: FileSystemService,
) : BazelEventHandler {
    private val fileConverter = FileConverter()
    private val testStatusConverter = TestStatusConverter()

    override fun handle(ctx: BazelEventHandlerContext): Boolean {
        if (!ctx.bazelEvent.hasTestResult()) {
            return false
        }

        val event = ctx.bazelEvent.testResult
        val id = ctx.bazelEvent.id
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            val status = testStatusConverter.convert(event.status)
            val testAttemptDurationMillis =
                event.testAttemptDuration
                    .let { Duration.ofSeconds(it.seconds, it.nanos.toLong()) }
                    .toMillis()
            ctx.onNext(
                ctx.messageFactory.createMessage(
                    ctx
                        .buildMessage()
                        .append("${id.testResult.label} test:")
                        .append(" $status ".apply(status.toColor()))
                        .append(", details: \"${event.statusDetails}\"".apply(Color.Details), Verbosity.Verbose)
                        .append(
                            ", attempt: ${id.testResult.attempt}, runs: ${id.testResult.run}, shard: ${id.testResult.shard}, duration: $testAttemptDurationMillis(ms), cached locally: ${event.cachedLocally}"
                                .apply(
                                    Color.Details,
                                ),
                            Verbosity.Verbose,
                        ).toString(),
                ),
            )
        }

        val hasNextAttempt = ctx.bazelEvent.childrenList.isNotEmpty()
        for (test in event.testActionOutputList.map { fileConverter.convert(it) }) {
            if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                ctx.onNext(ctx.messageFactory.createMessage("$test".apply(Color.Items)))
            }

            val content = test.readLines(ctx)
            traceFile(ctx, test, content)

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

                        ctx.onNext(ctx.messageFactory.createImportData("junit", testTempFile.absolutePath))
                    }
                }

                "log" ->
                    // check that it is last attempt
                    if (!hasNextAttempt) {
                        for (line in content) {
                            val message = ctx.messageFactory.createMessage(line)
                            // Allows to pass TeamCity services messages from tests` stdOut
                            if (line.contains("##teamcity")) {
                                message.addTag("tc:parseServiceMessagesInside")
                            }

                            ctx.onNext(message)
                        }
                    }
            }
        }

        return true
    }

    private fun traceFile(
        ctx: BazelEventHandlerContext,
        file: File,
        content: List<String>,
    ) {
        if (ctx.verbosity.atLeast(Verbosity.Diagnostic)) {
            ctx.messageFactory.createTraceMessage("File \"$file\":")
            for (line in content) {
                ctx.onNext(ctx.messageFactory.createTraceMessage("$\t$line"))
            }
        }
    }
}
