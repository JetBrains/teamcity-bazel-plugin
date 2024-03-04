

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
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append(event.stdout)
                                    .toString()
                    ))
                }

                if (event.stderr.isNotBlank()) {
                    for (errItem in decompose(event.stderr)) {
                        ctx.onNext(ctx.messageFactory.createMessage(
                                ctx.buildMessage()
                                        .append(errItem.text)
                                        .toString()
                        ))
                    }
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)

    private fun decompose(text: String): List<MessageItem> {
        return text.split('\n').map { toMessageItem(it) }
    }

    private fun toMessageItem(text: String): MessageItem {
        for ((regex, color) in prefixColors) {
            regex.find(text)?.let {
                val prefix = it.groupValues[1]
                val message = it.groupValues[2]
                return MessageItem("${prefix.apply(color)} $message", prefix, message, color)
            }
        }

        return MessageItem(text, "", text, Color.Default)
    }

    private data class MessageItem(val text: String, val prefix: String, val originalText: String, val color: Color)

    companion object {
        private val prefixColors = mapOf(
                "^(ERROR:)\\s*(.+)".toRegex() to Color.Error,
                "^(FAILED:)\\s*(.+)".toRegex() to Color.Error,
                "^(FAIL:)\\s*(.+)".toRegex() to Color.Error,
                "^(Action failed to execute:)\\s*(.+)".toRegex() to Color.Error,
                "^(WARNING:)\\s*(.+)".toRegex() to Color.Warning,
                "^(FLAKY:)\\s*(.+)".toRegex() to Color.Warning,
                "^(Auto-Configuration Warning:)\\s*(.+)".toRegex() to Color.Warning,
                "^(DEBUG:)\\s*(.+)".toRegex() to Color.Trace,
                "^(INFO:)\\s*(.+)".toRegex() to Color.Success,
                "^(Analyzing:)\\s*(.+)\$".toRegex() to Color.Success,
                "^(Building:)\\s*(.+)\$".toRegex() to Color.Success,
                "^(\\[\\s*\\d+\\s*\\/\\s*\\d+\\s*\\])\\s*\\[\\s*\\-+\\s*\\]\\s*(.+)\$".toRegex() to Color.Success,
                "^(\\[\\s*\\d+\\s*\\/\\s*\\d+\\s*\\])\\s*(.+)\$".toRegex() to Color.Success
        )
    }
}