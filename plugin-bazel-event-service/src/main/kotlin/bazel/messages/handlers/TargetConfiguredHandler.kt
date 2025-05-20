package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.Id
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class TargetConfiguredHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext): Boolean {
        val payload = ctx.event.payload
        return if (payload is BazelEvent && payload.rawEvent.hasConfigured()) {
            val event = payload.rawEvent.configured
            val id = payload.rawEvent.id
            val targetName =
                ctx
                    .buildMessage(
                        false,
                    ).append("Target ${event.targetKind} \"${id.targetConfigured.label}\"".apply(Color.BuildStage))
                    .toString()
            ctx.hierarchy.createNode(Id(id), payload.rawEvent.childrenList.map { Id(it) }, targetName)
            if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append(targetName)
                            .append(" configured")
                            .append(
                                ", aspect \"${id.targetConfigured.aspect}\", test size \"${event.testSize.name}\"",
                                Verbosity.Verbose,
                            ) { id.targetConfigured.aspect.isNotBlank() }
                            .append(
                                ", tags: \"${event.tagList.joinToStringEscaped(", ")}\"",
                                Verbosity.Verbose,
                            ) { event.tagList.isNotEmpty() }
                            .toString(),
                    ),
                )
            }

            true
        } else {
            ctx.handlerIterator.next().handle(ctx)
        }
    }
}
