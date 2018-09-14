package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.*
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class ActionExecutedHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is ActionExecuted) {
            val event = ctx.event.payload.content
            if (ctx.verbosity.atLeast(Verbosity.Minimal)) {
                val details = ctx.buildMessage(false)
                        .append(": ${event.cmdLines.joinToStringEscaped()}", Verbosity.Detailed)
                        .append("exit code: ${event.exitCode}", Verbosity.Detailed)
                        .append(", primary output: ${event.primaryOutput.name}", Verbosity.Detailed)
                        .append(", stdout: ${event.stdout.name}", Verbosity.Detailed)
                        .append(", stderr: ${event.stderr.name}", Verbosity.Detailed)
                        .toString()

                if (event.success) {
                    val description = "Action ${event.type} executed"
                    ctx.onNext(ctx.messageFactory.createBuildStatus(description))
                    if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                        ctx.onNext(ctx.messageFactory.createMessage(
                                ctx.buildMessage()
                                        .append(description.apply(Color.BuildStage))
                                        .append(details, Verbosity.Detailed)
                                        .toString()))
                    }
                } else {
                    val description = "Action ${event.type} failed to execute"
                    ctx.onNext(ctx.messageFactory.createErrorMessage(
                            ctx.buildMessage()
                                    .append(description.apply(Color.Error))
                                    .append(details, Verbosity.Detailed)
                                    .toString()))

                    ctx.onNext(ctx.messageFactory.createBuildProblem(description, ctx.event.projectId, ctx.event.payload.content.id.toString()))
                }
            }


            true
        } else ctx.handlerIterator.next().handle(ctx)
}