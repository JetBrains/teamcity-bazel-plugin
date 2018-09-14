package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.BuildFinished
import bazel.bazel.events.BuildStarted
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class BuildCompletedHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is BuildFinished) {
            val event = ctx.event.payload.content
            if (event.exitCode == 0) {
                val description = "Build finished"
                ctx.onNext(ctx.messageFactory.createBuildStatus(description))
                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append(description.apply(Color.BuildStage))
                                    .append(" with exit code ${event.exitCode}")
                                    .append("(${event.exitCodeName})", Verbosity.Detailed)
                                    .toString()))
                }
            }
            else {
                ctx.onNext(ctx.messageFactory.createBuildProblem(
                        ctx.buildMessage(false)
                                .append("Build failed")
                                .append(" with exit code ${event.exitCode}", Verbosity.Normal)
                                .append(" - ${event.exitCodeName}", Verbosity.Detailed)
                                .toString(),
                        ctx.event.projectId,
                        event.id.toString()))
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}