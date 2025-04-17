

package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.BazelEvent
import bazel.bazel.events.TargetConfigured
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class TargetConfiguredHandler : EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
            if (ctx.event.payload is BazelEvent && ctx.event.payload.content is TargetConfigured) {
                val event = ctx.event.payload.content
                val targetName = ctx.buildMessage(false).append("Target ${event.targetKind} \"${event.label}\"".apply(Color.BuildStage)).toString()
                ctx.hierarchy.createNode(event.id, event.children, targetName)
                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append(targetName)
                                    .append(" configured")
                                    .append(", aspect \"${event.aspect}\", test size \"${event.testSize}\"", Verbosity.Verbose) { event.aspect.isNotBlank() }
                                    .append(", tags: \"${event.tags.joinToStringEscaped(", ")}\"", Verbosity.Verbose) { event.tags.isNotEmpty() }
                                    .toString()))
                }

                true
            } else ctx.handlerIterator.next().handle(ctx)
}