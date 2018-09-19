package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.TestResult
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply
import java.io.File
import java.net.URI

class TestResultHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.High

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is TestResult) {
            val event = ctx.event.payload.content
            if(ctx.verbosity.atLeast(Verbosity.Detailed)) {
                ctx.onNext(ctx.messageFactory.createMessage(
                        ctx.buildMessage()
                                .append("${event.label} test:")
                                .append(" ${event.status} ".apply(event.status.toColor()))
                                .append(", details: \"${event.statusDetails}\"".apply(Color.Details), Verbosity.Verbose)
                                .append(", attempts: ${event.attempt}, runs: ${event.run}, shard: ${event.shard}, duration: ${event.testAttemptDurationMillis}(ms), cached locally: ${event.cachedLocally}".apply(Color.Details), Verbosity.Verbose)
                                .toString()))
            }

            for (test in event.testActionOutput) {
                val file = File(URI(test.uri))
                if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                    ctx.onNext(ctx.messageFactory.createMessage("$file".apply(Color.Items)))
                }

                if (file.name.endsWith(".xml", true)) {
                    if (!file.exists()) {
                        ctx.onNext(ctx.messageFactory.createMessage("File \"$file\" does not exist".apply(Color.Warning)))
                    }

                    file.setLastModified(System.currentTimeMillis())
                    ctx.onNext(ctx.messageFactory.createImportData("junit", file.absolutePath))
                }
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}