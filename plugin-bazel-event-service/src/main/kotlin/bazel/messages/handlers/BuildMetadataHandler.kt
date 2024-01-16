

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.BuildMetadata
import bazel.bazel.events.Configuration
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class BuildMetadataHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is BuildMetadata) {
                val event = ctx.event.payload.content
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    if (ctx.verbosity.atLeast(Verbosity.Verbose)) {
                        for (item in event.metadata) {
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