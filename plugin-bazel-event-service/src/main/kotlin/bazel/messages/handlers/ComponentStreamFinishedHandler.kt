package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.atLeast
import bazel.events.ComponentStreamFinished
import bazel.events.FinishType
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class ComponentStreamFinishedHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is ComponentStreamFinished) {
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when(ctx.event.payload.finishType) {
                FinishType.Finished ->
                    if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                        val description = "Component \"${ctx.event.payload.streamId.component}\" stream finished"
                        ctx.onNext(ctx.messageFactory.createBuildStatus(description))
                        ctx.onNext(ctx.messageFactory.createMessage(
                                ctx.buildMessage()
                                        .append(description.apply(Color.BuildStage))
                                        .append(", invocation: \"${ctx.event.payload.streamId.invocationId}\", build: \"${ctx.event.payload.streamId.buildId}\"", Verbosity.Detailed)
                                        .toString()))
                    }

                FinishType.Expired ->
                    if (ctx.verbosity.atLeast(Verbosity.Minimal)) {
                        val description = "Component \"${ctx.event.payload.streamId.component}\" stream expired"
                        ctx.onNext(ctx.messageFactory.createMessage(
                                ctx.buildMessage()
                                        .append(description.apply(Color.Warning))
                                        .append("(${FinishType.Expired.description}), invocation: \"${ctx.event.payload.streamId.invocationId}\", build: \"${ctx.event.payload.streamId.buildId}\"", Verbosity.Detailed)
                                        .toString()))

                        ctx.onNext(ctx.messageFactory.createBuildProblem(description, ctx.event.projectId, ctx.event.payload.streamId.toString()))
                    }
            }

            true
        } else ctx.handlerIterator.next().handle(ctx)
}