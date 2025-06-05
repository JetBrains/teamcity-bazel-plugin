package bazel.handlers.grpc

import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.BuildStatusFormatter
import bazel.messages.Color
import bazel.messages.MessageFactory.createErrorMessage
import bazel.messages.MessageFactory.createMessage
import bazel.messages.apply
import com.google.devtools.build.v1.BuildStatus.Result.*

class BuildFinishedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): HandlerResult {
        if (!ctx.event.hasBuildFinished()) {
            return notHandled()
        }

        return handled(
            sequence {
                val buildFinished = ctx.event.buildFinished
                val description = BuildStatusFormatter.format(buildFinished.status.result)
                when (buildFinished.status.result) {
                    COMMAND_SUCCEEDED -> {
                        yield(
                            createMessage(
                                buildString {
                                    append(ctx.messagePrefix)
                                    append(description.apply(Color.Success))
                                },
                            ),
                        )
                    }

                    CANCELLED,
                    COMMAND_FAILED,
                    SYSTEM_ERROR,
                    USER_ERROR,
                    RESOURCE_EXHAUSTED,
                    INVOCATION_DEADLINE_EXCEEDED,
                    REQUEST_DEADLINE_EXCEEDED,
                    -> {
                        yield(createErrorMessage(description))
                    }

                    else -> {}
                }
            },
        )
    }
}
