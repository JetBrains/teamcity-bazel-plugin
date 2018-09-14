package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.bazel.events.*
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class TargetCompletedHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Medium

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BazelEvent && ctx.event.payload.content is TargetComplete) {
            val event = ctx.event.payload.content

            if (event.success) {
                val description = "Target ${event.label} completed"
                if (ctx.verbosity.atLeast(Verbosity.Normal)) {
                    ctx.onNext(ctx.messageFactory.createBuildStatus(description))
                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append(description.apply(Color.BuildStage))
                                    .append(", test timeout: ${event.testTimeoutSeconds}(seconds)", Verbosity.Detailed)
                                    .append(", tags: \"${event.tags.joinToStringEscaped(", ")}\"", Verbosity.Detailed)
                                    .toString()))
                }
                else {
                    ctx.onNext(ctx.messageFactory.createBuildStatus(description))
                }
            }
            else {
                ctx.onNext(ctx.messageFactory.createErrorMessage(
                        ctx.buildMessage()
                                .append("Target ${event.label} \"${event.label}\" failed".apply(Color.Error))
                                .append(", test timeout: ${event.testTimeoutSeconds}(seconds)", Verbosity.Detailed)
                                .append(", tags: \"${event.tags.joinToStringEscaped(", ")}\"", Verbosity.Detailed)
                                .toString()))
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}