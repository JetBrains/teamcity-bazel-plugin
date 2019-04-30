package bazel.messages.handlers

import bazel.FileSystem
import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.TestResult
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply
import java.io.File

class TestResultHandler(
        private val _fileSystem: FileSystem)
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
                    val path = test.path
                    if (path == null) {
                        ctx.onNext(ctx.messageFactory.createMessage("Invalid file \"${test.name}\": \"${test.uri}\"".apply(Color.Warning)))
                        continue
                    }

                    if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                        ctx.onNext(ctx.messageFactory.createMessage("$path".apply(Color.Items)))
                    }

                    if (!_fileSystem.exists(path)) {
                        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                            ctx.onNext(ctx.messageFactory.createMessage("File \"$path\" does not exist.".apply(Color.Warning)))
                        }

                        continue
                    }

                    when (path.extension.toLowerCase()) {
                        "xml" -> {
                            traceFile(ctx, path)

                            // check that it is last attempt
                            if (!hasNextAttempt) {
                                // import test results
                                path.setLastModified(System.currentTimeMillis())
                                ctx.onNext(ctx.messageFactory.createImportData("junit", path.absolutePath))
                            }
                        }

                        "log" ->
                            // check that it is last attempt
                            if (!hasNextAttempt) {
                                for (line in _fileSystem.readFile(path)) {
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

    private fun traceFile(ctx: ServiceMessageContext, file: File) {
        if (ctx.verbosity.atLeast(Verbosity.Diagnostic)) {
            ctx.messageFactory.createTraceMessage("File \"${file.canonicalPath}\":")
            for (line in _fileSystem.readFile(file)) {
                ctx.onNext(ctx.messageFactory.createTraceMessage("$\t$line"))
            }
        }
    }
}