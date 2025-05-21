package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.converters.FileConverter
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.Id
import bazel.bazel.events.read
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class ActionExecutedHandler : EventHandler {
    private val fileConverter = FileConverter()

    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        return if (ctx.event.payload is BazelEvent && payload.event.hasAction()) {
            val event = payload.event.action
            val actionName = "Action \"${event.type}\""
            ctx.hierarchy.createNode(Id(payload.event.id), payload.event.childrenList.map { Id(it) }, actionName)

            val details = StringBuilder()
            details.appendLine(event.commandLineList.joinToStringEscaped().trim())

            var content = fileConverter.convert(event.primaryOutput).read(ctx)
            if (content.isNotBlank()) {
                details.appendLine(content)
            }

            content = fileConverter.convert(event.stdout).read(ctx)
            if (content.isNotBlank()) {
                details.appendLine(content)
            }

            content = fileConverter.convert(event.stderr).read(ctx)
            if (content.isNotBlank()) {
                details.appendLine(content.apply(Color.Error))
            }

            details.appendLine("Exit code: ${event.exitCode}")

            if (event.success) {
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    ctx.onNext(
                        ctx.messageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append(actionName.apply(Color.BuildStage))
                                .append(" executed.")
                                .toString(),
                        ),
                    )

                    ctx.onNext(
                        ctx.messageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append(details.toString())
                                .toString(),
                        ),
                    )
                }
            } else {
                val error =
                    ctx
                        .buildMessage(false)
                        .append(actionName)
                        .append(" failed to execute.")
                        .toString()

                ctx.onNext(
                    ctx.messageFactory.createBuildProblem(
                        error,
                        ctx.event.projectId,
                        payload.event.id.toString(),
                    ),
                )

                ctx.onNext(
                    ctx.messageFactory.createErrorMessage(
                        ctx
                            .buildMessage()
                            .append(details.toString())
                            .toString(),
                    ),
                )
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
    }
}
