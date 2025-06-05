package bazel.handlers.grpc

import bazel.Verbosity
import bazel.atLeast
import bazel.handlers.GrpcEventHandler
import bazel.handlers.GrpcEventHandlerContext
import bazel.handlers.HandlerResult
import bazel.handlers.HandlerResult.Companion.handled
import bazel.handlers.HandlerResult.Companion.notHandled
import bazel.messages.BuildStatusFormatter
import bazel.messages.Color
import bazel.messages.MessageFactory
import bazel.messages.MessageFactory.createErrorMessage
import bazel.messages.MessageFactory.createFlowFinished
import bazel.messages.apply
import com.google.devtools.build.v1.BuildStatus

class InvocationAttemptFinishedHandler : GrpcEventHandler {
    override fun handle(ctx: GrpcEventHandlerContext): HandlerResult {
        if (!ctx.event.hasInvocationAttemptFinished()) {
            return notHandled()
        }

        return handled(
            sequence {
                yield(createFlowFinished(ctx.streamId.invocationId))

                val invocationAttemptFinished = ctx.event.invocationAttemptFinished
                val status =
                    if (invocationAttemptFinished.hasInvocationStatus()) {
                        invocationAttemptFinished.invocationStatus.result
                    } else {
                        BuildStatus.Result.UNKNOWN_STATUS
                    }

                if (status == BuildStatus.Result.COMMAND_SUCCEEDED) {
                    if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                        yield(
                            MessageFactory.createMessage(
                                buildString {
                                    append(ctx.messagePrefix)
                                    append("Invocation attempt completed".apply(Color.Success))
                                },
                            ),
                        )
                    }
                } else {
                    val description = BuildStatusFormatter.format(status)
                    yield(
                        createErrorMessage(
                            buildString {
                                append("Invocation attempt failed")
                                if (ctx.verbosity.atLeast(Verbosity.Detailed)) {
                                    append(": \"$description\"")
                                }
                            },
                        ),
                    )
                }
            },
        )
    }
}
