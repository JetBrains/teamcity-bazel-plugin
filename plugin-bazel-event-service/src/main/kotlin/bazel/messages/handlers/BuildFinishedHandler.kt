package bazel.messages.handlers

import bazel.events.BuildStatus
import bazel.messages.BuildEventHandlerContext
import bazel.messages.Color
import bazel.messages.apply
import bazel.v1.converters.BuildStatusConverter

class BuildFinishedHandler : EventHandler {
    private val buildStatusConverter = BuildStatusConverter()

    override fun handle(ctx: BuildEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildFinished()) {
            return false
        }

        val buildFinished = ctx.event.buildFinished
        val status = buildStatusConverter.convert(buildFinished.status)
        when (status) {
            BuildStatus.CommandSucceeded -> {
                ctx.onNext(
                    ctx.messageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append(status.description.apply(Color.Success))
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
                ctx.onNext(ctx.messageFactory.createErrorMessage(status.description))
            }

            else -> {}
        }
        return true
    }
}
