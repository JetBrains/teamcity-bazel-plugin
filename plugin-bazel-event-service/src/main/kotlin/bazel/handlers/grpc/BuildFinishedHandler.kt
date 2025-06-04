package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.BuildStatusFormatter
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.apply
import bazel.messages.buildMessage
import com.google.devtools.build.v1.BuildStatus

class BuildFinishedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildFinished()) {
            return false
        }

        val buildFinished = ctx.event.buildFinished
        val description = BuildStatusFormatter.format(buildFinished.status.result)
        when (buildFinished.status.result) {
            BuildStatus.Result.COMMAND_SUCCEEDED -> {
                ctx.emitMessage(
                    MessageFactory.createMessage(
                        ctx
                            .buildMessage()
                            .append(description.apply(Color.Success))
                            .toString(),
                    ),
                )
            }

            BuildStatus.Result.CANCELLED,
            BuildStatus.Result.COMMAND_FAILED,
            BuildStatus.Result.SYSTEM_ERROR,
            BuildStatus.Result.USER_ERROR,
            BuildStatus.Result.RESOURCE_EXHAUSTED,
            BuildStatus.Result.INVOCATION_DEADLINE_EXCEEDED,
            BuildStatus.Result.REQUEST_DEADLINE_EXCEEDED,
            -> {
                ctx.emitMessage(MessageFactory.createErrorMessage(description))
            }

            else -> {}
        }
        return true
    }
}
