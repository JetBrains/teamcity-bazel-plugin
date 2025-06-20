package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.messages.BuildStatusFormatter
import bazel.messages.Color
import bazel.messages.apply
import com.google.devtools.build.v1.BuildStatus.Result.*

class BuildFinishedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): Boolean {
        if (!ctx.event.hasBuildFinished()) {
            return false
        }

        val buildFinished = ctx.event.buildFinished
        val description = BuildStatusFormatter.format(buildFinished.status.result)
        when (buildFinished.status.result) {
            COMMAND_SUCCEEDED -> ctx.writer.message(description.apply(Color.Success))
            CANCELLED,
            COMMAND_FAILED,
            SYSTEM_ERROR,
            USER_ERROR,
            RESOURCE_EXHAUSTED,
            INVOCATION_DEADLINE_EXCEEDED,
            REQUEST_DEADLINE_EXCEEDED,
            -> ctx.writer.error(description, hasPrefix = false)

            else -> {}
        }

        return true
    }
}
