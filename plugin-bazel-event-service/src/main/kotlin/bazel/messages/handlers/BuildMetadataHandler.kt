package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class BuildMetadataHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        return if (payload is BazelEvent && payload.event.hasBuildMetadata()) {
            val event = payload.event.buildMetadata
            if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                for (item in event.metadataMap) {
                    ctx.onNext(
                        ctx.messageFactory.createMessage(
                            ctx
                                .buildMessage()
                                .append(listOf(item.key, item.value).joinToStringEscaped(" = ").apply(Color.Items))
                                .toString(),
                        ),
                    )
                }
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
    }
}
