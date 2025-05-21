package bazel.messages.handlers

import bazel.events.BuildStatus
import bazel.messages.Color
import bazel.messages.ServiceMessageContext
import bazel.messages.apply
import bazel.v1.converters.BuildStatusConverter

class BuildFinishedHandler : EventHandler {
    private val buildStatusConverter = BuildStatusConverter()

    override fun handle(ctx: ServiceMessageContext): Boolean {
        if (!ctx.event.rawEvent.hasBuildFinished()) {
            return false
        }

        val buildFinished = ctx.event.rawEvent.buildFinished
        val result = buildStatusConverter.convert(buildFinished.status)
        val status = result.status.description
        when (result.status) {
            BuildStatus.CommandSucceeded -> {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append(status.apply(Color.Success))
                            .toString(),
                    ),
                )
            }

            BuildStatus.Cancelled,
            BuildStatus.CommandFailed,
            BuildStatus.SystemError,
            BuildStatus.UserError,
            BuildStatus.ResourceExhausted,
            BuildStatus.InvocationDeadlineExceeded,
            BuildStatus.RequestDeadlineExceeded,
            -> {
                ctx.onNext(ctx.messageFactory.createErrorMessage(status))
            }

            else -> {}
        }
        return true
    }
}
