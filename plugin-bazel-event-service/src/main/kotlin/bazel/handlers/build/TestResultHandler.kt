package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.file.File
import bazel.file.FileConverter
import bazel.file.FileSystemService
import bazel.file.readLines
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.TestStatusConverter
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.toColor
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
        val id = ctx.event.id
        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            val status = testStatusConverter.convert(event.status)
            val testAttemptDurationMillis =
                event.testAttemptDuration
                    .let { Duration.ofSeconds(it.seconds, it.nanos.toLong()) }
                    .toMillis()
            ctx.onNext(
                MessageFactory.createMessage(
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

        val hasNextAttempt = ctx.event.childrenList.isNotEmpty()
        for (test in event.testActionOutputList.map { fileConverter.convert(it) }) {
            if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                ctx.onNext(MessageFactory.createMessage("$test".apply(Color.Items)))
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

                        ctx.onNext(MessageFactory.createImportData("junit", testTempFile.absolutePath))
                    }
                }

                "log" ->
                    // check that it is last attempt
                    if (!hasNextAttempt) {
                        for (line in content) {
                            val message = MessageFactory.createMessage(line)
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
        ctx: BuildEventHandlerContext,
        file: File,
        content: List<String>,
    ) {
        if (ctx.verbosity.atLeast(Verbosity.Diagnostic)) {
            MessageFactory.createTraceMessage("File \"$file\":")
            for (line in content) {
                ctx.onNext(MessageFactory.createTraceMessage("$\t$line"))
            }
        }
    }
}
