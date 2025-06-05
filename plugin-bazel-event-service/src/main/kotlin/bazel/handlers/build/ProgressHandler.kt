package bazel.handlers.build

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.BuildEventHandler
import bazel.handlers.BuildEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.Color
import bazel.messages.MessageFactory.createErrorMessage
import bazel.messages.MessageFactory.createMessage
import bazel.messages.MessageFactory.createTraceMessage
import bazel.messages.MessageFactory.createWarningMessage
import bazel.messages.apply

class ProgressHandler : BuildEventHandler {
    override fun handle(ctx: BuildEventHandlerContext): HandlerResult {
        if (!ctx.event.hasProgress()) {
            return notHandled()
        }
        return handled(
            sequence {
                val progress = ctx.event.progress
                if (ctx.verbosity.atLeast(Verbosity.Normal) && progress.stdout.isNotBlank()) {
                    yield(
                        createMessage(
                            buildString {
                                append(ctx.messagePrefix)
                                append(progress.stdout)
                            },
                        ),
                    )
                }

                if (progress.stderr.isNotBlank()) {
                    val decomposedMessages = decompose(progress.stderr)
                    val mostSevereLevel =
                        decomposedMessages
                            .map { it.level }
                            .maxByOrNull { it.value } ?: LogLevel.Normal

                    for (errItem in decomposedMessages) {
                        val messageText =
                            buildString {
                                append(ctx.messagePrefix)
                                append(errItem.text)
                            }

                        val message =
                            when (mostSevereLevel) {
                                LogLevel.Error -> createErrorMessage(messageText)
                                LogLevel.Warning -> createWarningMessage(messageText)
                                LogLevel.Normal -> createMessage(messageText)
                                LogLevel.Trace -> createTraceMessage(messageText)
                            }

                        yield(message)
                    }
                }
            },
        )
    }

    private fun decompose(text: String): List<MessageItem> = text.split('\n').map { toMessageItem(it) }

    private fun toMessageItem(text: String): MessageItem {
        for ((regex, logLevel, prefixColor) in prefixConfigs) {
            regex.find(text)?.let {
                val prefix = it.groupValues[1]
                val message = it.groupValues[2]
                val color = prefixColor ?: logLevel.defaultColor
                return MessageItem("${prefix.apply(color)} $message", prefix, message, logLevel)
            }
        }

        return MessageItem(text, "", text, LogLevel.Normal)
    }

    private data class MessageItem(
        val text: String,
        val prefix: String,
        val originalText: String,
        val level: LogLevel,
    )

    private enum class LogLevel(
        val value: Int,
        val defaultColor: Color,
    ) {
        Error(3, Color.Error),
        Warning(2, Color.Warning),
        Normal(1, Color.Default),
        Trace(0, Color.Trace),
    }

    private data class MessageConfig(
        val prefixRegex: Regex,
        val level: LogLevel,
        val prefixColor: Color? = null,
    )

    companion object {
        private val prefixConfigs =
            listOf(
                MessageConfig("^(ERROR:)\\s*(.+)".toRegex(), LogLevel.Error),
                MessageConfig("^(FAILED:)\\s*(.+)".toRegex(), LogLevel.Error),
                MessageConfig("^(FAIL:)\\s*(.+)".toRegex(), LogLevel.Error),
                MessageConfig("^(Action failed to execute:)\\s*(.+)".toRegex(), LogLevel.Error),
                MessageConfig("^(WARNING:)\\s*(.+)".toRegex(), LogLevel.Warning),
                MessageConfig("^(FLAKY:)\\s*(.+)".toRegex(), LogLevel.Warning),
                MessageConfig("^(Auto-Configuration Warning:)\\s*(.+)".toRegex(), LogLevel.Warning),
                MessageConfig("^(DEBUG:)\\s*(.+)".toRegex(), LogLevel.Trace),
                MessageConfig("^(INFO:)\\s*(.+)".toRegex(), LogLevel.Normal, Color.Success),
                MessageConfig("^(Analyzing:)\\s*(.+)\$".toRegex(), LogLevel.Normal, Color.Success),
                MessageConfig("^(Building:)\\s*(.+)\$".toRegex(), LogLevel.Normal, Color.Success),
                MessageConfig(
                    "^(\\[\\s*\\d+\\s*\\/\\s*\\d+\\s*\\])\\s*\\[\\s*\\-+\\s*\\]\\s*(.+)\$".toRegex(),
                    LogLevel.Normal,
                    Color.Success,
                ),
                MessageConfig(
                    "^(\\[\\s*\\d+\\s*\\/\\s*\\d+\\s*\\])\\s*(.+)\$".toRegex(),
                    LogLevel.Normal,
                    Color.Success,
                ),
            )
    }
}
