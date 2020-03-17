package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.ActionExecuted
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.File
import bazel.bazel.events.read
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply
import bazel.messages.logError
import java.io.InputStreamReader

class ActionExecutedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is ActionExecuted) {
                val event = ctx.event.payload.content
                val actionName = "Action \"${event.type}\""
                ctx.hierarchy.createNode(event.id, event.children, actionName)

                val details = StringBuilder()
                details.appendln(event.cmdLines.joinToStringEscaped().trim())

                var content = event.primaryOutput.read(ctx)
                if (content.isNotBlank()) {
                    details.appendln(content)
                }

                content = event.stdout.read(ctx)
                if (content.isNotBlank()) {
                    details.appendln(content)
                }

                content = event.stderr.read(ctx)
                if (content.isNotBlank()) {
                    details.appendln(content.apply(Color.Error))
                }

                details.appendln("Exit code: ${event.exitCode}")

                if (event.success) {
                    if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                        ctx.onNext(ctx.messageFactory.createMessage(
                                ctx.buildMessage()
                                        .append(actionName.apply(Color.BuildStage))
                                        .append(" executed.")
                                        .toString()))

                        ctx.onNext(ctx.messageFactory.createMessage(
                                ctx.buildMessage()
                                        .append(details.toString())
                                        .toString()))
                    }
                } else {
                    val error = ctx.buildMessage(false)
                            .append(actionName)
                            .append(" failed to execute.")
                            .toString()

                    ctx.onNext(ctx.messageFactory.createBuildProblem(
                            error,
                            ctx.event.projectId,
                            ctx.event.payload.content.id.toString()))

                    ctx.onNext(ctx.messageFactory.createErrorMessage(
                            ctx.buildMessage()
                                    .append(details.toString())
                                    .toString()))
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)

}