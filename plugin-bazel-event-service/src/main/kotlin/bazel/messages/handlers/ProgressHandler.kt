package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.Progress
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class ProgressHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.High

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is Progress) {
            val event = ctx.event.payload.content
            if (ctx.verbosity.atLeast(Verbosity.Normal) && event.stdout.isNotBlank()) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append(event.stdout)
                            .toString(),
                    ),
                )
            }

            if (event.stderr.isNotBlank()) {
                val decomposedMessages = decompose(event.stderr)
                val mostSevereLevel =
                    decomposedMessages
                        .map { it.level }
                        .maxByOrNull { it.value } ?: LogLevel.Normal

                for (errItem in decomposedMessages) {
                    val messageText =
                        ctx
                            .buildMessage()
                            .append(errItem.text)
                            .toString()

                    val message =
                        ctx.messageFactory.let {
                            when (mostSevereLevel) {
                                LogLevel.Error -> it.createErrorMessage(messageText)
                                LogLevel.Warning -> it.createWarningMessage(messageText)
                                LogLevel.Normal -> it.createMessage(messageText)
                                LogLevel.Trace -> it.createTraceMessage(messageText)
                            }
                        }
                    ctx.onNext(message)
                }
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
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
