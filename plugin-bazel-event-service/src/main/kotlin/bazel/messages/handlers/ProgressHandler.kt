

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
                        if (errItem.state.failed) {
                            ctx.onNext(ctx.messageFactory.createBuildProblem(
                                    "${errItem.prefix} ${errItem.originalText}",
                                    ctx.event.projectId,
                                    ctx.event.payload.content.id.toString()
                            ))
                        } else {
                            ctx.onNext(ctx.messageFactory.createMessage(
                                    ctx.buildMessage()
                                            .append(errItem.text)
                                            .toString()
                            ))
                        }
                    }
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)

    private fun decompose(text: String): List<MessageItem> {
        return text.split('\n').map { toMessageItem(it) }
    }

    private fun toMessageItem(text: String): MessageItem {
        for ((regex, state) in prefixColors) {
            regex.find(text)?.let {
                val prefix = it.groupValues[1]
                val message = it.groupValues[2]
                return MessageItem("${prefix.apply(state.color)} $message", prefix, message, state)
            }
        }

        return MessageItem(text, "", text, State(Color.Default))
    }

    private data class MessageItem(val text: String, val prefix: String, val originalText: String, val state: State)
    private data class State(val color: Color, val failed: Boolean = false)

    companion object {
        private val prefixColors = mapOf<Regex, State>(
                "^(ERROR:) (No test targets were found, yet testing was requested)".toRegex() to State(Color.Error, false),
                "^(ERROR:)\\s*(.+)".toRegex() to State(Color.Error, true),
                "^(FAILED:)\\s*(.+)".toRegex() to State(Color.Error, true),
                "^(FAIL:)\\s*(.+)".toRegex() to State(Color.Error),
                "^(Action failed to execute:)\\s*(.+)".toRegex() to State(Color.Error, true),
                "^(WARNING:)\\s*(.+)".toRegex() to State(Color.Warning),
                "^(FLAKY:)\\s*(.+)".toRegex() to State(Color.Warning),
                "^(Auto-Configuration Warning:)\\s*(.+)".toRegex() to State(Color.Warning),
                "^(DEBUG:)\\s*(.+)".toRegex() to State(Color.Trace),
                "^(INFO:)\\s*(.+)".toRegex() to State(Color.Success),
                "^(Analyzing:)\\s*(.+)\$".toRegex() to State(Color.Success),
                "^(Building:)\\s*(.+)\$".toRegex() to State(Color.Success),
                "^(\\[\\s*\\d+\\s*\\/\\s*\\d+\\s*\\])\\s*\\[\\s*\\-+\\s*\\]\\s*(.+)\$".toRegex() to State(Color.Success),
                "^(\\[\\s*\\d+\\s*\\/\\s*\\d+\\s*\\])\\s*(.+)\$".toRegex() to State(Color.Success)
        )
    }
}