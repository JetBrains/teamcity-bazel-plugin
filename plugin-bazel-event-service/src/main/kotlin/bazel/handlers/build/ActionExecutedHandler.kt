package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.file.FileConverter
import bazel.file.read
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
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
            ctx.onNext(ctx.messageFactory.createCompilationStarted(error))
            ctx.onNext(
                ctx.messageFactory.createErrorMessage(
                    ctx
                        .buildMessage()
                        .append(details.toString())
                        .toString(),
                ),
            )
            ctx.onNext(ctx.messageFactory.createCompilationFinished(error))
        }

        return true
    }
}
