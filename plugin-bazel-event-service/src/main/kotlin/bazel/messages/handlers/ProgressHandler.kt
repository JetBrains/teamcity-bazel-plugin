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
                if (event.stdout.isNotBlank()) {
                    if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                        ctx.onNext(ctx.messageFactory.createMessage(
                                ctx.buildMessage()
                                        .append(event.stdout)
                                        .toString()
                        ))
                    }
                }

                if (event.stderr.isNotBlank()) {
                    for (err in decompose(event.stderr)) {
                        ctx.onNext(ctx.messageFactory.createMessage(
                                ctx.buildMessage()
                                        .append(err)
                                        .toString()
                        ))
                    }
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)

    private fun decompose(text: String): List<String> {
        return text.split('\n').map { applyColor(it) }
    }

    private fun applyColor(text: String): String {
        for ((prefix, color) in prefixColors) {
            if (text.startsWith(prefix)) {
                return prefix.apply(color) + text.substring(prefix.length)
            }
        }

        return text
    }

    companion object {
        private val prefixColors = mapOf<String, Color>(
                "ERROR:" to Color.Error,
                "FAILED:" to Color.Error,
                "Action failed to execute:" to Color.Error,
                "WARNING:" to Color.Warning,
                "INFO:" to Color.Success
        )
    }
}