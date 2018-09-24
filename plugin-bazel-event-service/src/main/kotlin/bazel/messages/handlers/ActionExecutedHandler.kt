package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.ActionExecuted
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.File
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply
import java.net.URI

class ActionExecutedHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is ActionExecuted) {
                val event = ctx.event.payload.content
                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    val actionName = "Action \"${event.type}\""
                    val details = StringBuilder()
                    details.appendln(event.cmdLines.joinToStringEscaped().trim())
                    details.appendln("Exit code: ${event.exitCode}")
                    var content = readFromFile(event.primaryOutput)
                    if (content.isNotBlank()) {
                        details.appendln(content)
                    }

                    content = readFromFile(event.stdout)
                    if (content.isNotBlank()) {
                        details.appendln(content)
                    }

                    content = readFromFile(event.stderr)
                    if (content.isNotBlank()) {
                        details.appendln(content)
                    }


                    if (event.success) {
                        ctx.hierarchy.createNode(event.id, event.children, actionName)
                        ctx.onNext(ctx.messageFactory.createBuildStatus(actionName))
                        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                            ctx.onNext(ctx.messageFactory.createMessage(
                                    ctx.buildMessage()
                                            .append(actionName.apply(Color.BuildStage))
                                            .append(" executed")
                                            .append(details.toString().apply(Color.Details), Verbosity.Verbose)
                                            .toString()))
                        }
                    } else {
                        ctx.onNext(ctx.messageFactory.createBuildProblem(
                                ctx.buildMessage(false)
                                        .append(actionName)
                                        .append(" failed to execute ")
                                        .append(details.toString())
                                        .toString(),
                                ctx.event.projectId,
                                ctx.event.payload.content.id.toString()))
                    }
                }


                true
            } else ctx.handlerIterator.next().handle(ctx)


    private fun readFromFile(file: File): String {
        if (file.uri.isBlank()) {
            return ""
        }

        return java.io.File(URI(file.uri)).readText()
    }
}