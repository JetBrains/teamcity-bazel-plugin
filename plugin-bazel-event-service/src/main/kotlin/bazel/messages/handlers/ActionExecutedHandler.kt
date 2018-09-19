package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.ActionExecuted
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class ActionExecutedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is ActionExecuted) {
                val event = ctx.event.payload.content
                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    val details = ctx.buildMessage(false)
                            .append(": ${event.cmdLines.joinToStringEscaped()}", Verbosity.Verbose)
                            .append("exit code: ${event.exitCode}", Verbosity.Verbose)
                            .append(", primary output: ${event.primaryOutput.name}", Verbosity.Verbose)
                            .append(", stdout: ${event.stdout.name}", Verbosity.Verbose)
                            .append(", stderr: ${event.stderr.name}", Verbosity.Verbose)
                            .toString()

                    if (event.success) {
                        val description = "Action ${event.type} executed"
                        ctx.onNext(ctx.messageFactory.createBuildStatus(description))
                        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                            ctx.onNext(ctx.messageFactory.createMessage(
                                    ctx.buildMessage()
                                            .append(description.apply(Color.BuildStage))
                                            .append(details, Verbosity.Verbose)
                                            .toString()))
                        }
                    } else {
                        val description = "Action ${event.type} failed to execute"
                        ctx.onNext(ctx.messageFactory.createErrorMessage(
                                ctx.buildMessage()
                                        .append(description.apply(Color.Error))
                                        .append(details, Verbosity.Verbose)
                                        .toString()))

                        ctx.onNext(ctx.messageFactory.createBuildProblem(description, ctx.event.projectId, ctx.event.payload.content.id.toString()))
                    }
                }


                true
            } else ctx.handlerIterator.next().handle(ctx)
}