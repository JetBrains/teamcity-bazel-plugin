package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.file.File
import bazel.file.FileConverter
import bazel.file.FileSystemService
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.MessageFactory.createErrorMessage
import bazel.messages.MessageFactory.createMessage
import bazel.messages.MessageFactory.createTraceMessage
import bazel.messages.TestStatusConverter
import bazel.messages.apply
import bazel.messages.toColor
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.Duration

class TestResultHandler(
    private val _fileSystemService: FileSystemService,
) : BuildEventHandler {
    private val fileConverter = FileConverter()
    private val testStatusConverter = TestStatusConverter()

    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasTestResult()) {
            return notHandled()
        }

        return handled(
            sequence {
                val event = ctx.event.testResult
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    val id = ctx.event.id
                    val status = testStatusConverter.convert(event.status)
                    val testAttemptDurationMillis =
                        event.testAttemptDuration
                            .let { Duration.ofSeconds(it.seconds, it.nanos.toLong()) }
                            .toMillis()
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
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
                                        ).apply(
                                            Color.Details,
                                        ),
                                    )
                                }
                            },
                        ),
                    )
                }

                val hasNextAttempt = ctx.event.childrenList.isNotEmpty()
                for (test in event.testActionOutputList.map { fileConverter.convert(it) }) {
                    if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                        yield(createMessage("$test".apply(Color.Items)))
                    }

                    val content = readFileLines(test, ctx.verbosity)
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

                                yield(MessageFactory.createImportData("junit", testTempFile.absolutePath))
                            }
                        }

                        "log" ->
                            // check that it is last attempt
                            if (!hasNextAttempt) {
                                for (line in content) {
                                    val message = createMessage(line)
                                    // Allows to pass TeamCity services messages from tests` stdOut
                                    if (line.contains("##teamcity")) {
                                        message.addTag("tc:parseServiceMessagesInside")
                                    }

                                    yield(message)
                                }
                            }
                    }
                }
            },
        )
    }

    private suspend fun SequenceScope<ServiceMessage>.readFileLines(
        file: File,
        verbosity: Verbosity,
    ): List<String> {
        try {
            val content = InputStreamReader(file.createStream()).use { it.readLines() }
            if (verbosity.atLeast(Verbosity.Diagnostic)) {
                traceFile(file, content)
            }
            return content
        } catch (ex: Exception) {
            if (verbosity.atLeast(Verbosity.Diagnostic)) {
                yield(createErrorMessage("Cannot read from ${file.name}.", ex.toString()))
            }
            return emptyList()
        }
    }

    private suspend fun SequenceScope<ServiceMessage>.traceFile(
        file: File,
        content: List<String>,
    ) {
        createTraceMessage("File \"$file\":")
        for (line in content) {
            yield(createTraceMessage("$\t$line"))
        }
    }
}
