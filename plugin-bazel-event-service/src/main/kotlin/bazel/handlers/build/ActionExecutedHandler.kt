package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.file.File
import bazel.file.FileConverter
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createCompilationFinished
import bazel.messages.MessageFactory.createCompilationStarted
import bazel.messages.MessageFactory.createErrorMessage
import bazel.messages.MessageFactory.createMessage
import bazel.messages.apply
import bazel.messages.joinToStringEscaped
import com.google.devtools.build.lib.buildeventstream.BuildEventStreamProtos
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.io.InputStreamReader

class ActionExecutedHandler : BuildEventHandler {
    private val fileConverter = FileConverter()

    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasAction()) {
            return notHandled()
        }

        return handled(
            sequence {
                val event = ctx.event.action
                val actionName = "Action \"${event.type}\""

                if (!event.success) {
                    val error = "$actionName failed to execute."
                    val details = getActionDetails(event, ctx.verbosity)
                    yield(createCompilationStarted(error))
                    yield(
                        createErrorMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append(details)
                            },
                        ),
                    )
                    yield(createCompilationFinished(error))
                    return@sequence
                }

                if (!ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    return@sequence
                }
                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append(actionName.apply(Color.BuildStage))
                            append(" executed.")
                        },
                    ),
                )

                val details = getActionDetails(event, ctx.verbosity)
                yield(
                    createMessage(
                        buildString {
                            append(ctx.messagePrefix)
                            append(details)
                        },
                    ),
                )
            },
        )
    }

    private suspend fun SequenceScope<ServiceMessage>.getActionDetails(
        event: BuildEventStreamProtos.ActionExecuted,
        verbosity: Verbosity,
    ) = buildString {
        suspend fun SequenceScope<ServiceMessage>.appendFileIfNotBlank(
            file: BuildEventStreamProtos.File,
            isError: Boolean = false,
        ) {
            val content = readFile(fileConverter.convert(file), verbosity).trim()
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

    private suspend fun SequenceScope<ServiceMessage>.readFile(
        file: File,
        verbosity: Verbosity,
    ): String {
        try {
            return InputStreamReader(file.createStream()).use { it.readText() }
        } catch (ex: Exception) {
            if (verbosity.atLeast(Verbosity.Diagnostic)) {
                yield(createErrorMessage("Cannot read from ${file.name}.", ex.toString()))
            }
            return ""
        }
    }
}
