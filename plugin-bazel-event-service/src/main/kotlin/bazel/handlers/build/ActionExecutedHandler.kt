package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.file.File
import bazel.file.FileConverter
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.MessageWriter
import bazel.messages.apply
import bazel.messages.joinToStringEscaped
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import java.io.InputStreamReader

class ActionExecutedHandler : BuildEventHandler {
    private val fileConverter = FileConverter()

    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasAction()) {
            return false
        }

        val event = ctx.event.action
        val actionName = "Action \"${event.type}\""

        if (!event.success) {
            val error = "$actionName failed to execute."
            ctx.writer.compilationStarted(error)
            val details = getActionDetails(event, ctx.verbosity, ctx.writer)
            ctx.writer.error(details, hasPrefix = false)
            ctx.writer.compilationFinished(error)
            return true
        }

        if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
            ctx.writer.message(actionName.apply(Color.BuildStage) + " executed.")
            ctx.writer.message(getActionDetails(event, ctx.verbosity, ctx.writer))
        }

        return true
    }

    private fun getActionDetails(
        event: BuildEventStreamProtos.ActionExecuted,
        verbosity: Verbosity,
        writer: MessageWriter,
    ) = buildString {
        fun appendFileIfNotBlank(
            file: BuildEventStreamProtos.File,
            isError: Boolean = false,
        ) {
            val content = readFile(fileConverter.convert(file), verbosity, writer).trim()
            if (content.isNotEmpty()) {
                appendLine(if (isError) content.apply(Color.Error) else content)
            }
        }

        appendLine(event.commandLineList.joinToStringEscaped().trim())
        appendFileIfNotBlank(event.primaryOutput)
        appendFileIfNotBlank(event.stdout)
        appendFileIfNotBlank(event.stderr, isError = true)
        appendLine("Exit code: ${event.exitCode}")
    }

    private fun readFile(
        file: File,
        verbosity: Verbosity,
        writer: MessageWriter,
    ): String {
        try {
            return InputStreamReader(file.createStream()).use { it.readText() }
        } catch (ex: Exception) {
            if (verbosity.atLeast(Verbosity.Diagnostic)) {
                writer.error("Cannot read from ${file.name}.", errorDetails = ex.toString())
            }
            return ""
        }
    }
}
