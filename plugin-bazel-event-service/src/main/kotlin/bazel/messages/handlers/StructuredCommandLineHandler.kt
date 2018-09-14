package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.*
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class StructuredCommandLineHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is StructuredCommandLine) {
            val commandLine = ctx.event.payload.content
            ctx.onNext(ctx.messageFactory.createBuildStatus("Run ${commandLine.commandLineLabel}"))
            if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                ctx.onNext(ctx.messageFactory.createMessage(
                        ctx.buildMessage()
                                .append("Run \"")
                                .append(commandLine.commandLineLabel.apply(Color.BuildStage))
                                .append("\"")
                            .toString())) }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}