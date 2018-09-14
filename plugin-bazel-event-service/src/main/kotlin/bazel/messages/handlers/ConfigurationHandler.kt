package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.*
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class ConfigurationHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is Configuration) {
            val event = ctx.event.payload.content
            if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                ctx.onNext(ctx.messageFactory.createMessage(ctx.buildMessage().append(listOf("platformName", event.platformName).joinToStringEscaped(" = ").apply(Color.Items)).toString()))
                ctx.onNext(ctx.messageFactory.createMessage(ctx.buildMessage().append(listOf("mnemonic", event.mnemonic).joinToStringEscaped(" = ").apply(Color.Items)).toString()))
                ctx.onNext(ctx.messageFactory.createMessage(ctx.buildMessage().append(listOf("cpu", event.cpu).joinToStringEscaped(" = ").apply(Color.Items)).toString()))

                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    for (item in event.makeVariableMap) {
                        ctx.onNext(ctx.messageFactory.createMessage(
                                ctx.buildMessage()
                                        .append(listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items))
                                        .toString()))
                    }
                }
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}