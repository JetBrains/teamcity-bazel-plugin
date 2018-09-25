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
                        if (errItem.color == Color.Success || errItem.color == Color.Error) {
                            ctx.onNext(ctx.messageFactory.createBuildStatus(errItem.originalText))
                        }

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
        for ((prefix, color) in prefixColors) {
            if (text.startsWith(prefix)) {
                val origText = text.substring(prefix.length)
                return MessageItem(prefix.apply(color) + origText, origText, color)
            }
        }

        return MessageItem(text, text, Color.Default)
    }

    private data class MessageItem(val text: String, val originalText: String, val color: Color)

    companion object {
        private val prefixColors = mapOf<String, Color>(
                "ERROR:" to Color.Error,
                "FAILED:" to Color.Error,
                "Action failed to execute:" to Color.Error,
                "WARNING:" to Color.Warning,
                "Auto-Configuration Warning:" to Color.Warning,
                "DEBUG:" to Color.Details,
                "INFO:" to Color.Success,
                "Analyzing:" to Color.Success
        )
    }
}