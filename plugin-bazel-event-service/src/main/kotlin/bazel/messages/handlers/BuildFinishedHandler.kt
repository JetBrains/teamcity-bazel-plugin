package bazel.messages.handlers

import bazel.HandlerPriority
import bazel.Verbosity
import bazel.events.BuildFinished
import bazel.events.BuildStatus
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply

class BuildFinishedHandler: EventHandler {
    override val priority: HandlerPriority
        get() = HandlerPriority.Low

    override fun handle(ctx: ServiceMessageContext) =
        if (ctx.event.payload is BuildFinished) {
            val status = ctx.event.payload.result.status.description
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (ctx.event.payload.result.status) {
                BuildStatus.CommandSucceeded -> {
                    ctx.onNext(ctx.messageFactory.createBuildStatus(status))

                    ctx.onNext(ctx.messageFactory.createMessage(
                            ctx.buildMessage()
                                    .append(status.apply(Color.Success))
                                    .toString()))
                }

                BuildStatus.Cancelled,
                BuildStatus.CommandFailed,
                BuildStatus.SystemError,
                BuildStatus.UserError,
                BuildStatus.ResourceExhausted,
                BuildStatus.InvocationDeadlineExceeded,
                BuildStatus.RequestDeadlineExceeded ->
                    ctx.onNext(ctx.messageFactory.createBuildProblem(
                            status,
                            ctx.event.projectId,
                            "Build:${ctx.event.payload.result.status}"))
            }
            true
        } else ctx.handlerIterator.next().handle(ctx)
}