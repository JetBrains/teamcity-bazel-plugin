

package bazel.messages.handlers

import bazel.FileSystemService
import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.TestResult
import bazel.bazel.events.readLines
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply
import java.io.File
import java.io.FileWriter
import java.io.OutputStreamWriter

class TestResultHandler(
        private val _fileSystemService: FileSystemService)
    : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.High

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is TestResult) {
                val event = ctx.event.payload.content
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append("${event.label} test:")
                                    .append(" ${event.status} ".apply(event.status.toColor()))
                                    .append(", details: \"${event.statusDetails}\"".apply(Color.Details), Verbosity.Verbose)
                                    .append(", attempt: ${event.attempt}, runs: ${event.run}, shard: ${event.shard}, duration: ${event.testAttemptDurationMillis}(ms), cached locally: ${event.cachedLocally}".apply(Color.Details), Verbosity.Verbose)
                                    .toString()))
                }

                val hasNextAttempt = event.children.isNotEmpty()
                for (test in event.testActionOutput) {
                    if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                        ctx.onNext(ctx.messageFactory.createMessage("${test}".apply(Color.Items)))
                    }

                    val content = test.readLines(ctx)
                    traceFile(ctx, test, content)

                    if (test.name.length < 3) {
                        continue
                    }

                    when (test.name.toLowerCase().takeLast(3)) {
                        "xml" -> {
                            // check that it is last attempt
                            if (!hasNextAttempt) {
                                // import test results
                                val testTempFile = _fileSystemService.generateTempFile("tmp", test.name)
                                _fileSystemService.write(testTempFile) {
                                    OutputStreamWriter(it).use {
                                        for (line in content) {
                                            it.write(line)
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

                true
            } else ctx.handlerIterator.next().handle(ctx)

    private fun traceFile(ctx: ServiceMessageContext, file: bazel.bazel.events.File, content: List<String>) {
        if (ctx.verbosity.atLeast(Verbosity.Diagnostic)) {
            ctx.messageFactory.createTraceMessage("File \"$file\":")
            for (line in content) {
                ctx.onNext(ctx.messageFactory.createTraceMessage("$\t$line"))
            }
        }
    }
}