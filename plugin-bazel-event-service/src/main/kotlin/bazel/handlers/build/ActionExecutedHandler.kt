package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.file.FileConverter
import bazel.file.read
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage
import bazel.messages.joinToStringEscaped

class ActionExecutedHandler : BuildEventHandler {
    private val fileConverter = FileConverter()

    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasAction()) {
            return false
        }

        val event = ctx.event.action
        val actionName = "Action \"${event.type}\""
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
                    MessageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append(actionName.apply(Color.BuildStage))
                            .append(" executed.")
                            .toString(),
                    ),
                )

                ctx.onNext(
                    MessageFactory.createMessage(
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
            ctx.onNext(MessageFactory.createCompilationStarted(error))
            ctx.onNext(
                MessageFactory.createErrorMessage(
                    ctx
                        .buildMessage()
                        .append(details.toString())
                        .toString(),
                ),
            )
            ctx.onNext(MessageFactory.createCompilationFinished(error))
        }

        return true
    }
}
